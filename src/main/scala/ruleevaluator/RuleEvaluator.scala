package ruleevaluator

import ruleevaluator.calculator.Calculator
import ruleevaluator.combiner.TokenCombiner
import ruleevaluator.csv.{Csv, CsvFileParser}
import ruleevaluator.rule.Result
import ruleevaluator.rulesfile.{RulesFileContent, RuleLine, RulesFileParser}
import ruleevaluator.scanner.Scanner
import ruleevaluator.token.Token

import java.io.File

class RuleEvaluator(val conditions: String, val csv: String) {
  private val DELIMITER = "\t"
  private val LINE_BREAK = "\n"

  def run(): Unit = {
    val conditionsFile = RulesFileParser.parse(new File(conditions))
    val parsedcsv = CsvFileParser.parse(new File(csv))
    val result = RuleEvaluator.calculate(conditionsFile, parsedcsv)
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
  def calculate(conditions: RulesFileContent, parsedcsv: Csv): Result = {
    conditions.lines
      .map(line => parseAndCombineTokens(line, parsedcsv))
      .map(Calculator.calculate)
      .fold(Result.PASS)((a, b) => a combineWithAnd b)
  }

  private def parseAndCombineTokens(line: RuleLine, csv: Csv): List[Token] = {
    val tokens = new Scanner(line, csv).parseTokens()
    new TokenCombiner(tokens, line.lineNumber).combineTokensToConditions()
  }
}
