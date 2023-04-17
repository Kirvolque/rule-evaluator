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

/**
 * The main entry point of the RuleEvaluator program. Parses command line arguments to extract the paths to the CSV
 * file and the rule file, reads the files, and evaluates the rules against each row of the CSV file. Prints out the
 * evaluation results for each row to the console.
 */
object Main extends ZIOAppDefault {

  /**
   * Represents the configuration options that are parsed from the command line arguments.
   *
   * @param csvFile  The path to the CSV file.
   * @param ruleFile The path to the rule file.
   */
  private case class Config(csvFile: String = "", ruleFile: String = "")

  override def run: ZIO[Any with ZIOAppArgs with Scope, Throwable, ExitCode] = for {
    config <- ZIOAppArgs.getArgs.map(_.toList).map(a => parseConfig(a))
    _ <- runApp(config.ruleFile, config.csvFile)
  } yield ExitCode.success


  /**
   * Parses the command line arguments to extract the paths to the CSV file and the rule file.
   *
   * @param args The command line arguments.
   * @return The configuration options parsed from the command line arguments.
   */
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

  /**
   * Runs the RuleEvaluator program by parsing the rule file, parsing the CSV file, evaluating the rules against each
   * row of the CSV file, and printing out the evaluation results for each row to the console.
   *
   * @param ruleFile The path to the rule file.
   * @param csvFile  The path to the CSV file.
   * @return A ZIO effect that represents the completion of the program.
   */
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
