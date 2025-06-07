package ruleevaluator

import cats.data.Validated.{Invalid, Valid}
import ruleevaluator.evaluator.Evaluator
import ruleevaluator.combiner.{TokenCombiner, TokenError}
import ruleevaluator.csv.{CsvFileParser, CsvRow}
import ruleevaluator.result.ResultMonoid._
import ruleevaluator.scanner.Scanner
import ruleevaluator.token.Token
import cats.implicits._
import ruleevaluator.exception.InvalidRuleSyntaxException
import ruleevaluator.result.{Result, ResultMonoid}
import ruleevaluator.rule.{RuleLine, RulesFileContent, RulesFileParser}
import ruleevaluator.token.Token.CombinedToken

/**
 * The `RuleEvaluator` object provides a function to check a set of conditions against a parsed CSV file using an `Evaluator`.
 *
 * The `checkRules` function takes in a `RulesFileContent` object, which contains the parsed contents of the rule file, and a `CsvRow`
 * object, which contains the parsed contents of the CSV file. The function then applies the rules to each row in the CSV file and
 * returns a `Result` object that contains the evaluation status for each row.
 */
object RuleEvaluator {

  /**
   * Checks the given set of conditions against the parsed CSV file using an `Evaluator`.
   *
   * @param conditions the `RulesFileContent` object containing the parsed contents of the rule file
   * @param parsedCsv  the `CsvRow` object containing the parsed contents of the CSV file
   * @return a `Result` object containing the evaluation status for each row in the CSV file
   */
  def checkRules(conditions: RulesFileContent, parsedCsv: CsvRow)(implicit andMonoid: ResultMonoid.AndMonoid): Result =
    conditions.lines
      .map(line => parseAndCombineTokens(line, parsedCsv))
      .map(Evaluator.evaluate)
      .combineAll(andMonoid)

  private def parseAndCombineTokens(line: RuleLine, csv: CsvRow): List[CombinedToken] = {
    val tokens = new Scanner(line, csv).parseTokens()
    TokenCombiner.combineTokens(tokens, line.lineNumber) match {
      case Valid(t) => t
      case Invalid(e) => throw new InvalidRuleSyntaxException(e)
    }
  }
}
