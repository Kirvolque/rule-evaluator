package ruleevaluator

import ruleevaluator.evaluator.Evaluator
import ruleevaluator.combiner.TokenCombiner
import ruleevaluator.csv.{Csv, CsvFileParser}
import ruleevaluator.rule.Result
import ruleevaluator.rule.ResultMonoid._
import ruleevaluator.rulesfile.{RulesFileContent, RuleLine, RulesFileParser}
import ruleevaluator.scanner.Scanner
import ruleevaluator.token.Token
import cats.implicits._

import java.io.File

class RuleEvaluator(val conditions: String, val csv: String) {
  private val DELIMITER = "\t"
  private val LINE_BREAK = "\n"

  // TODO move this function to a separate class. Refactor.
  def run(): Unit = {
    val conditionsFile = RulesFileParser.parse(new File(conditions))
    val parsedcsv = CsvFileParser.parse(new File(csv))
    val result = RuleEvaluator.checkRules(conditionsFile, parsedcsv)
    printResult(result.toString, getFailReasons(result))
  }

  private def getFailReasons(result: Result): String =
    if (result.successful) "" else result.failReasons.mkString("|")

  private def printResult(status: String, failReasons: String): Unit = {
    printOut("Status", "Fail Reason")
    printOut(status, failReasons)
  }

  private def printOut(values: String*): Unit =
    println(values.mkString(DELIMITER) + LINE_BREAK)

}

object RuleEvaluator {
  def checkRules(conditions: RulesFileContent, parsedcsv: Csv): Result =
    conditions.lines
      .map(line => parseAndCombineTokens(line, parsedcsv))
      .map(Evaluator.evaluate)
      .combineAll(andResultMonoid)

  private def parseAndCombineTokens(line: RuleLine, csv: Csv): List[Token] = {
    val tokens = new Scanner(line, csv).parseTokens()
    new TokenCombiner(tokens, line.lineNumber).combineTokensToConditions()
  }
}
