package ruleevaluator.rulesfile

import zio.stream.ZPipeline
import zio.{Task, ZIO, stream}
object RulesFileParser {
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
