import ruleevaluator.RuleEvaluator
import ruleevaluator.csv.CsvFileParser
import ruleevaluator.rulesfile.RulesFileParser
import ruleevaluator.evaluator.Evaluator
import ruleevaluator.combiner.TokenCombiner
import ruleevaluator.csv.{Csv, CsvFileParser}
import ruleevaluator.rule.Result
import ruleevaluator.rulesfile.{RuleLine, RulesFileContent, RulesFileParser}
import scopt.OptionParser

import java.io.File

object Main {
  private case class Config(csvFile: String = "", ruleFile: String = "")

  private val DELIMITER = "\t"
  private val LINE_BREAK = "\n"

  def main(args: Array[String]): Unit = {
    val parser = new OptionParser[Config]("RuleEvaluator") {
      opt[String]('c', "csv")
        .required()
        .valueName("<csvFile>")
        .action((x, c) => c.copy(csvFile = x))
        .text("Path to CSV file")
      opt[String]('r', "rule")
        .required()
        .valueName("<RuleFile>")
        .action((x, c) => c.copy(ruleFile = x))
        .text("Path to rule file")
    }

    parser.parse(args, Config()) match {
      case Some(config) =>
        println(s"CSV file name: ${config.csvFile}")
        println(s"Rule file name: ${config.ruleFile}")
        run(config.ruleFile, config.csvFile)
      case _ =>
      // arguments are bad, error message will have been displayed
    }
  }

  private def run(ruleFile: String, csvFile: String): Unit = {
    val conditionsFile = RulesFileParser.parse(new File(ruleFile))
    val parsedCsv = CsvFileParser.parse(new File(csvFile))
    val result = RuleEvaluator.checkRules(conditionsFile, parsedCsv)
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
