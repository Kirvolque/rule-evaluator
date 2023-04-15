import ruleevaluator.RuleEvaluator
import ruleevaluator.csv.CsvFileParser
import ruleevaluator.rulesfile.RulesFileParser
import ruleevaluator.evaluator.Evaluator
import ruleevaluator.combiner.TokenCombiner
import ruleevaluator.csv.{CsvRow, CsvFileParser}
import ruleevaluator.rule.Result
import ruleevaluator.rulesfile.{RuleLine, RulesFileContent, RulesFileParser}
import scopt.OptionParser

import java.io.File

object Main {
  private case class Config(csvFile: String = "", ruleFile: String = "")

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
    CsvFileParser.parse(new File(csvFile))
      .zipWithIndex
      .foreach((row, index) => {
        val result = RuleEvaluator.checkRules(conditionsFile, row)
        println(s"row: ${index + 1} status: $result")
      })
  }
}
