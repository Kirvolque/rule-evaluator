package ruleevaluator.rule

import zio.stream.ZPipeline
import zio.{Task, ZIO, stream}

/**
 * The `RulesFileParser` object provides functionality for parsing a rules file into a `RulesFileContent` object.
 */
object RulesFileParser {

  /**
   * Parses the rules file with the specified file name and returns a `Task` that will produce the corresponding `RulesFileContent`.
   *
   * @param fileName the name of the rules file to parse
   * @return a `Task` that will produce the corresponding `RulesFileContent`
   */
  def parse(fileName: String): ZIO[Any, Throwable, RulesFileContent] =
    stream.ZStream
      .fromFileName(fileName)
      .via(ZPipeline.utf8Decode >>> ZPipeline.splitLines)
      .runCollect
      .map(lines => {
        val rules = lines.zipWithIndex.toList.map { (line, index) =>
          new RuleLine(index + 1, line)
        }
        new RulesFileContent(rules)
      })
}
