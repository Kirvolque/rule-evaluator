import ruleevaluator.RuleEvaluator
import ruleevaluator.csv.{CsvConstants, CsvFileParser, CsvRow}
import ruleevaluator.rulesfile.RulesFileParser
import ruleevaluator.evaluator.Evaluator
import ruleevaluator.combiner.TokenCombiner
import ruleevaluator.rule.Result
import ruleevaluator.rulesfile.{RuleLine, RulesFileContent, RulesFileParser}
import scopt.OptionParser
import zio.stream.ZStream
import zio.{Console, ZIO, ZIOApp}
import zio._

import scala.io.Source
import scala.util.Using

object Main extends ZIOAppDefault  {
  private case class Config(csvFile: String = "", ruleFile: String = "")

  override def run: ZIO[Any with ZIOAppArgs with Scope, Throwable, ExitCode] = for {
    config <- ZIOAppArgs.getArgs.map(_.toList).map(a => parseConfig(a))
    _ <- runApp(config.ruleFile, config.csvFile)
  } yield ExitCode.success


  private def parseConfig(args: List[String]): Config = {
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
        config
      case _ => throw IllegalArgumentException()
      // arguments are bad, error message will have been displayed
    }
  }

  private def runApp(ruleFile: String, csvFile: String): ZIO[Any with Scope, Throwable, Unit] = {
    RulesFileParser.parse(ruleFile)
      .map(parsedRules => {
        CsvFileParser.parse(csvFile)
          .map(csv => RuleEvaluator.checkRules(parsedRules, csv))
          .foreach(a => Console.printLine(a))
      }).flatten
  }
}
