package ruleevaluator.csv

import scala.io.Source
import java.io.File
import ruleevaluator.csv.CsvRow

object CsvFileParser {

  def parse(file: File): Iterator[CsvRow] = {
    val lines = Source.fromFile(file = file).getLines()
    val header = lines.next().split(CsvConstants.DELIMITER).toList
    lines.map(line => {
      val row = line.split(CsvConstants.DELIMITER)
      CsvRow(header.zip(row).toMap)
    })
  }
}
