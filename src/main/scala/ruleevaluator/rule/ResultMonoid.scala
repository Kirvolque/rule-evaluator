package ruleevaluator.rule

import cats.Monoid

object ResultMonoid {
  implicit val andResultMonoid: Monoid[Result] = new Monoid[Result] {
    def empty: Result = Result.PASS

    def combine(x: Result, y: Result): Result =
      val mergedFailReasons = x.failReasons.union(y.failReasons)
      Result(x.successful && y.successful, mergedFailReasons)
  }

  implicit val orResultMonoid: Monoid[Result] = new Monoid[Result] {
    def empty: Result = Result.FAIL

    def combine(x: Result, y: Result): Result =
      val mergedResultIsSuccessful = x.successful || y.successful
      val mergedFailReasons =
        if (mergedResultIsSuccessful) Set.empty[String]
        else x.failReasons.union(y.failReasons)
      Result(mergedResultIsSuccessful, mergedFailReasons)
  }
}