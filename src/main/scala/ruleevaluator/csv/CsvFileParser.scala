package ruleevaluator.csv

import scala.io.Source
import java.io.File
import ruleevaluator.csv.CsvRow

object CsvFileParser {
  private val DELIMITER: String = ","

  def parse(file: File): CsvRow = {
    val lines = Source.fromFile(file = file).getLines().toList
    val header = lines.head.split(DELIMITER).toList
    val line = lines.tail.head.split(DELIMITER).toList
    val content = header.zip(line).toMap
    CsvRow(content)
  }
}
