package ruleevaluator

import ruleevaluator.evaluator.Evaluator
import ruleevaluator.combiner.TokenCombiner
import ruleevaluator.csv.{CsvRow, CsvFileParser}
import ruleevaluator.result.ResultMonoid._
import ruleevaluator.rulesfile.{RulesFileContent, RuleLine, RulesFileParser}
import ruleevaluator.scanner.Scanner
import ruleevaluator.token.Token
import cats.implicits._
import ruleevaluator.result.Result

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
  def checkRules(conditions: RulesFileContent, parsedCsv: CsvRow): Result =
    conditions.lines
      .map(line => parseAndCombineTokens(line, parsedCsv))
      .map(Evaluator.evaluate)
      .combineAll(andResultMonoid)

  private def parseAndCombineTokens(line: RuleLine, csv: CsvRow): List[Token] = {
    val tokens = new Scanner(line, csv).parseTokens()
    new TokenCombiner(tokens, line.lineNumber).combineTokensToConditions()
  }
}
