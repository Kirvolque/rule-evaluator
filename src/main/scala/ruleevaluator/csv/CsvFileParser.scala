package ruleevaluator.csv

import scala.io.{BufferedSource, Source}
import java.io.File
import ruleevaluator.csv.CsvRow

object CsvFileParser {

  def parse(csvSource: BufferedSource): Iterator[CsvRow] = {
    val lines = csvSource.getLines()
    val header = lines.next().split(CsvConstants.DELIMITER).toList
    lines.map(line => {
      val row = line.split(CsvConstants.DELIMITER)
      CsvRow(header.zip(row).toMap)
    })
  }
}
