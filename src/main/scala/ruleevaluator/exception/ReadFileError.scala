package ruleevaluator.exception

sealed trait ReadFileError extends Throwable

object ReadFileError {

  case class FileNotFound(cause: Throwable) extends ReadFileError {
    override def getMessage: String = s"File not found: ${cause.getMessage}"
    override def getCause: Throwable = cause
  }

  case class InvalidContent(details: String) extends ReadFileError {
    override def getMessage: String = s"Invalid content: $details"
  }

  case class Other(cause: Throwable) extends ReadFileError {
    override def getMessage: String = s"Unknown error: ${cause.getMessage}"
    override def getCause: Throwable = cause
  }
}