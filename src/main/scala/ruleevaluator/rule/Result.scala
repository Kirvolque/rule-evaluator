package ruleevaluator.rule

import ruleevaluator.token.{Argument, Token}

class Result(val successful: Boolean, val failReasons: Set[String]) {

  override def toString: String = if (successful) "Pass" else "Fail"
}

object Result {

  val PASS: Result = Result(true, Set.empty)
  val FAIL: Result = Result(false, Set.empty)
  
  def fail(tokens: Iterable[Token]): Result = {
    val values = tokens.filter(token => token.isInstanceOf[Argument.CsvField]) // TODO refactor
      .map(_.asInstanceOf[Argument.CsvField])
      .map(_.name)
      .toSet
    Result(false, values)
  }
}
