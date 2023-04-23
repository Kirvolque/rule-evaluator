package ruleevaluator.result

import ruleevaluator.result.Result
import ruleevaluator.token.{Argument, Token}

/**
 * Represents the result of evaluating a rule.
 *
 * @param successful  Whether the rule evaluation was successful or not.
 * @param failReasons The reasons why the rule evaluation failed.
 */
class Result(val successful: Boolean, val failReasons: Set[String]) {

  override def toString: String = if (successful)
    "Pass" else s"Fail: ${failReasons.mkString(" | ")}"
}

object Result {

  val PASS: Result = Result(true, Set.empty)
  val FAIL: Result = Result(false, Set.empty)

  /**
   * Constructs a failed Result instance based on the given tokens.
   *
   * @param tokens The tokens that caused the rule evaluation to fail.
   * @return A failed Result instance with the appropriate failReasons.
   */
  def fail(tokens: Iterable[Token]): Result = {
    val values = tokens
      .collect { case Argument.CsvField(name, _) => name }
      .toSet
    Result(false, values)
  }
}
