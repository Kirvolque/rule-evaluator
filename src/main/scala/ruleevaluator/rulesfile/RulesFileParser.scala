package ruleevaluator.rulesfile

import java.io.{BufferedReader, File, FileReader}
import scala.util.Using

object RulesFileParser {
  def parse(fileName: String): RulesFileContent = {
    Using(io.Source.fromFile(fileName)) { source => {
      val lines = source.getLines()
      val rules = lines.zipWithIndex.map { case (line, index) =>
        new RuleLine(index + 1, line)
      }
      new RulesFileContent(rules.toList)
    }
    }.get
  }
}
