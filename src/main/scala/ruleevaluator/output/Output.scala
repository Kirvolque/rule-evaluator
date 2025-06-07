package ruleevaluator.output

import zio.{ZIO, ZLayer, Console}

trait Output {
  def printLine(line: String): ZIO[Any, Nothing, Unit]
}

object Output {
  val console: ZLayer[Any, Nothing, Output] =
    ZLayer.succeed(new Output {
      override def printLine(line: String): ZIO[Any, Nothing, Unit] =
        Console.printLine(line).orDie
    })
}