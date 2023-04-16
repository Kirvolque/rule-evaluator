package ruleevaluator.csv

import scala.io.{BufferedSource, Source}
import java.io.File
import ruleevaluator.csv.CsvRow
import zio.stream.{ZPipeline, ZStream}
import zio.{Scope, Task, UIO, ZIO, stream}

object CsvFileParser {

  def parse(fileName: String): ZStream[Any with Scope, Throwable, CsvRow] =
    stream.ZStream.fromIteratorZIO(stream.ZStream
      .fromFileName(fileName)
      .via(ZPipeline.utf8Decode >>> ZPipeline.splitLines)
      .toIterator
      .map(getRows))

  // TODO. Refactor
  private def getRows(lines: Iterator[Either[Throwable, String]]): Iterator[CsvRow] = {
    val header = getRow(lines.next()).split(CsvConstants.DELIMITER).toList
    lines.map(line => {
      val row = getRow(line).split(CsvConstants.DELIMITER)
      CsvRow(header.zip(row).toMap)
    })
  }

  private def getRow(either: Either[Throwable, String]) = {
    either match
      case Left(error) => throw error
      case Right(value) => value
  }
}
