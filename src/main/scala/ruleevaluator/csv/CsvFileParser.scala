package ruleevaluator.csv

import scala.io.{BufferedSource, Source}
import java.io.File
import ruleevaluator.csv.CsvRow
import zio.stream.{ZPipeline, ZStream}
import zio.{Scope, Task, UIO, ZIO, stream}

object CsvFileParser {

  /**
   * Parses a CSV file and returns a `ZStream` of `CsvRow` values.
   *
   * @param fileName the name of the CSV file to parse
   * @return a `ZStream` of `CsvRow` values
   */
  def parse(fileName: String): ZStream[Any with Scope, Throwable, CsvRow] =
    stream.ZStream.fromIteratorZIO(stream.ZStream
      .fromFileName(fileName)
      .via(ZPipeline.utf8Decode >>> ZPipeline.splitLines)
      .toIterator
      .map(_.flatMap(_.toSeq))
      .map(getRows))

  private def getRows(lines: Iterator[String]): Iterator[CsvRow] = {
    val header = lines.next().split(CsvConstants.DELIMITER).toList
    lines.map(line => {
      val row = line.split(CsvConstants.DELIMITER)
      CsvRow(header.zip(row).toMap)
    })
  }
}
