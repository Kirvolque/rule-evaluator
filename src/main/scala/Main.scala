import ruleevaluator.RuleEvaluator
import ruleevaluator.csv.{CsvConstants, CsvFileParser, CsvRow}
import ruleevaluator.rulesfile.RulesFileParser
import ruleevaluator.evaluator.Evaluator
import ruleevaluator.combiner.TokenCombiner
import ruleevaluator.result.Result
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
      case Some(config) => config
      case _ => throw IllegalArgumentException(s"Illegal arguments ${args.mkString(", ")}")
    }
  }

  private def runApp(ruleFile: String, csvFile: String): ZIO[Any with Scope, Throwable, Unit] = {
    RulesFileParser.parse(ruleFile)
      .map(parsedRules => {
        CsvFileParser.parse(csvFile)
          .map(csv => RuleEvaluator.checkRules(parsedRules, csv))
          .zipWithIndex
          .map((result, index) => s"row: ${index + 1} status: $result")
          .foreach(resultString => Console.printLine(resultString))
      }).flatten
  }
}
