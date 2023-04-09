package ruleevaluator.rule

import ruleevaluator.csv.CsvField
import ruleevaluator.token.{Token, TokenType}

class Result(val successful: Boolean, val failReasons: Set[String]) {

  def combineWithAnd(another: Result): Result = {
    val mergedFailReasons = failReasons.union(another.failReasons)
    Result(this.successful && another.successful, mergedFailReasons)
  }

  def combineWithOr(another: Result): Result = {
    val mergedResultIsSuccessful = this.successful || another.successful
    val mergedFailReasons =
      if (mergedResultIsSuccessful) Set.empty[String]
      else failReasons.union(another.failReasons)
    Result(mergedResultIsSuccessful, mergedFailReasons)
  }

  override def toString: String = if (successful) "Pass" else "Fail"
}

object Result {

  val PASS: Result = Result(true, Set.empty)
  val FAIL: Result = Result(false, Set.empty)

  def fail(tokens: Iterable[Token[Any]]): Result = {
    val fields = tokens.filter(_.hasType(TokenType.CSV_FIELD))
      .flatMap(_.value)
      .map(_.asInstanceOf[CsvField].name)
      .toSet
    Result(false, fields)
  }
}
