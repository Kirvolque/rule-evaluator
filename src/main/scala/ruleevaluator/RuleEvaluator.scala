package ruleevaluator

import ruleevaluator.evaluator.Evaluator
import ruleevaluator.combiner.TokenCombiner
import ruleevaluator.csv.{CsvRow, CsvFileParser}
import ruleevaluator.rule.Result
import ruleevaluator.rule.ResultMonoid._
import ruleevaluator.rulesfile.{RulesFileContent, RuleLine, RulesFileParser}
import ruleevaluator.scanner.Scanner
import ruleevaluator.token.Token
import cats.implicits._


object RuleEvaluator {
  def checkRules(conditions: RulesFileContent, parsedcsv: CsvRow): Result =
    conditions.lines
      .map(line => parseAndCombineTokens(line, parsedcsv))
      .map(Evaluator.evaluate)
      .combineAll(andResultMonoid)

  private def parseAndCombineTokens(line: RuleLine, csv: CsvRow): List[Token] = {
    val tokens = new Scanner(line, csv).parseTokens()
    new TokenCombiner(tokens, line.lineNumber).combineTokensToConditions()
  }
}
