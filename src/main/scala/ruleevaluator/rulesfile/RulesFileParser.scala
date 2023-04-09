package ruleevaluator.rulesfile

import java.io.{BufferedReader, File, FileReader}
import scala.jdk.StreamConverters._

object RulesFileParser {
  def parse(conditionsFile: File): RulesFileContent = {
    val lines = new BufferedReader(new FileReader(conditionsFile)).lines().toScala(List)
    val rules = lines.zipWithIndex.map { case (line, index) =>
      new RuleLine(index + 1, line)
    }
    new RulesFileContent(rules)
  }
}
