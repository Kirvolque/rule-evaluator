package ruleevaluator.rule

import ruleevaluator.exception.ReadFileError
import zio.stream.ZPipeline
import zio.{Chunk, Task, ZIO, stream}

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
  def parse(fileName: String): ZIO[Any, ReadFileError, RulesFileContent] =
    stream.ZStream
      .fromFileName(fileName)
      .via(ZPipeline.utf8Decode >>> ZPipeline.splitLines)
      .runCollect
      .mapError {
        case ex: java.io.FileNotFoundException => ReadFileError.FileNotFound(ex)
        case ex: Throwable => ReadFileError.Other(ex)
      }
      .map(parseLines)

  private def parseLines(lines: Chunk[String]): RulesFileContent = {
    val rules = lines.zipWithIndex.toList.map { (line, index) =>
      new RuleLine(index + 1, line)
    }
    new RulesFileContent(rules)
  }

}
