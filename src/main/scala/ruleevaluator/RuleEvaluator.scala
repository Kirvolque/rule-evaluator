package ruleevaluator

import cats.data.Validated.{Invalid, Valid}
import ruleevaluator.evaluator.Evaluator
import ruleevaluator.combiner.{TokenCombiner, TokenError}
import ruleevaluator.csv.{CsvFileParser, CsvRow}
import ruleevaluator.result.ResultMonoid.*
import ruleevaluator.scanner.Scanner
import ruleevaluator.token.Token
import ruleevaluator.exception.{InvalidRuleSyntaxException, RuleProcessingError}
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
   * This method never throws an exception and instead returns all errors encapsulated within `Either`.
   *
   * @param conditions the `RulesFileContent` object containing the parsed contents of the rule file.
   * @param parsedCsv  the `CsvRow` object containing the parsed contents of the CSV file.
   * @return an `Either[RuleProcessingError, Result]` where:
   *         - `Right(Result)` contains the evaluation results.
   *         - `Left(RuleProcessingError)` contains the encountered error (if any).
   */
  def checkRules(conditions: RulesFileContent, parsedCsv: CsvRow)
                (implicit andMonoid: ResultMonoid.AndMonoid): Either[RuleProcessingError, Result] = {
    try {
      conditions.lines
        .map(line => parseAndCombineTokens(line, parsedCsv))
        .collectFirst { case Left(error) => Left(error) }
        .getOrElse {
          val evaluations = conditions.lines.map { line =>
            parseAndCombineTokens(line, parsedCsv).map(Evaluator.evaluate)
          }
          evaluations.sequence.map(_.combineAll(andMonoid))
        }
    } catch {
      case e: Throwable =>
        Left(RuleProcessingError(s"Unexpected error during rule evaluation: ${e.getMessage}", Some(e)))
    }
  }

  /**
   * Parses and combines tokens for a given rule line and CSV row.
   * This method assumes that any errors related to tokenization and combination are handled as `Either`,
   * without throwing exceptions.
   *
   * @param line the `RuleLine` object containing a single line of rules.
   * @param csv  the `CsvRow` object representing a single row of the CSV.
   * @return an `Either[RuleProcessingError, List[CombinedToken]]`.
   */
  private def parseAndCombineTokens(line: RuleLine, csv: CsvRow): Either[RuleProcessingError, List[CombinedToken]] = {
    // Step 1: Parse tokens
    val tokens = new Scanner(line, csv).parseTokens()

    // Step 2: Combine tokens
    TokenCombiner.combineTokens(tokens, line.lineNumber) match {
      case Valid(combinedTokens) =>
        Right(combinedTokens) // Successfully combined tokens
      case Invalid(errors) =>
        Left(RuleProcessingError(
          s"Syntax error at line ${line.lineNumber}: ${errors.map(_.message).mkString(", ")}"
        ))
    }
  }
}