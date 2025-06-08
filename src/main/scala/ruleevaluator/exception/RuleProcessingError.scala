package ruleevaluator.exception

/**
 * A general error to represent failures during rule processing (e.g., parsing, evaluation, etc.).
 *
 * @param message The description of the error.
 * @param cause   The optional underlying exception that caused this error.
 */
case class RuleProcessingError(message: String, cause: Option[Throwable] = None) extends RuntimeException(message, cause.orNull)