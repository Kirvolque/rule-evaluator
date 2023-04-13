package ruleevaluator.rule

import cats.Monoid

/**
 *
 * This object provides two monoids for the Result class, which can be used for combining the results of rules.
 * A monoid is an algebraic structure that consists of a set, an operation, and an identity element. 
 * In the case of the Result class, the operation is combining two Result objects, and the identity element is either 
 * a successful Result with no fail reasons, or an unsuccessful Result with no fail reasons.
 * The andResultMonoid combines two Result objects using a logical "AND" operation. 
 * If both results are successful, the combined result will also be successful. 
 * If either result is unsuccessful, the combined result will be unsuccessful, 
 * and the fail reasons will be merged from both results.
 * The orResultMonoid combines two Result objects using a logical "OR" operation. 
 * If either result is successful, the combined result will be successful. 
 * If both results are unsuccessful, the fail reasons will be merged from both results.
 *
 * @see [[cats.Monoid]]
 */
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