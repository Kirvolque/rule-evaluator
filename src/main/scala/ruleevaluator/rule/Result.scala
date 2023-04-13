package ruleevaluator.rule

import ruleevaluator.token.{Argument, Token}

/**
 * Represents the result of evaluating a rule.
 *
 * @param successful  Whether the rule evaluation was successful or not.
 * @param failReasons The reasons why the rule evaluation failed.
 */
class Result(val successful: Boolean, val failReasons: Set[String]) {

  override def toString: String = if (successful) "Pass" else "Fail"
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
    val values = tokens.filter(token => token.isInstanceOf[Argument.CsvField]) // TODO refactor
      .map(_.asInstanceOf[Argument.CsvField])
      .map(_.name)
      .toSet
    Result(false, values)
  }
}
