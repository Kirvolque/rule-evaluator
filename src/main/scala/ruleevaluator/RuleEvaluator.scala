package ruleevaluator

import cats.data.Validated.{Invalid, Valid}
import ruleevaluator.evaluator.Evaluator
import ruleevaluator.combiner.{TokenCombiner, TokenError}
import ruleevaluator.csv.{CsvFileParser, CsvRow}
import ruleevaluator.result.ResultMonoid._
import ruleevaluator.scanner.Scanner
import ruleevaluator.token.Token
import ruleevaluator.exception.InvalidRuleSyntaxException
import ruleevaluator.result.{Result, ResultMonoid}
import ruleevaluator.rule.{RuleLine, RulesFileContent, RulesFileParser}
import ruleevaluator.token.Token.CombinedToken
import cats.implicits._

import scala.util.Try

/**
 * The `RuleEvaluator` object provides a function to check a set of conditions against a parsed CSV file using an `Evaluator`.
 *
 * The `checkRules` function takes in a `RulesFileContent` object, which contains the parsed contents of the rule file, and a `CsvRow`
 * object, which contains the parsed contents of the CSV file. The function then applies the rules to each row in the CSV file and
 * returns an `Either` object containing either the evaluation result (`Right`) or an error (`Left`).
 */
object RuleEvaluator {

  /**
   * Checks the given set of conditions against the parsed CSV file using an `Evaluator`.
   *
   * @param conditions the `RulesFileContent` object containing the parsed contents of the rule file
   * @param parsedCsv  the `CsvRow` object containing the parsed contents of the CSV file
   * @return an `Either[Throwable, Result]` where `Right` contains the evaluation results
   *         and `Left` contains the first encountered error.
   */
  def checkRules(conditions: RulesFileContent, parsedCsv: CsvRow)
                (implicit andMonoid: ResultMonoid.AndMonoid): Either[Throwable, Result] = {
    conditions.lines
      .map(line => parseAndCombineTokens(line, parsedCsv))
      .collectFirst { case Left(err) => Left(err) }
      .getOrElse {
        val evaluations = conditions.lines.map { line =>
          parseAndCombineTokens(line, parsedCsv).map(Evaluator.evaluate)
        }
        evaluations.sequence.map(_.combineAll(andMonoid))
      }
  }

  private def parseAndCombineTokens(line: RuleLine, csv: CsvRow): Either[Throwable, List[CombinedToken]] = {
    Try {
      val tokens = new Scanner(line, csv).parseTokens()
      TokenCombiner.combineTokens(tokens, line.lineNumber) match {
        case Valid(t)   => t
        case Invalid(e) => throw new InvalidRuleSyntaxException(e)
      }
    }.toEither
  }
}
