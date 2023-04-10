package ruleevaluator.evaluator

import ruleevaluator.rule.{Computable, Result}
import ruleevaluator.rule.ResultMonoid._
import ruleevaluator.token.Argument.CsvField
import ruleevaluator.token.{Argument, BasicToken, ComparisonOperator, LogicalOperator, Token}
import cats.Monoid

import scala.annotation.tailrec

object Evaluator {
  def evaluate(tokens: List[Token]): Result =
    splitBy[Token](tokens, token => token.isInstanceOf[LogicalOperator.Or.type])
      .map(list =>
        splitBy(list, token => token.isInstanceOf[LogicalOperator.And.type])
          .flatten
          .flatMap(convertToComputable)
          .map(_.compute())
          .fold(Result.PASS)((a, b) => Monoid.combine(a, b)(andResultMonoid))
      )
      .reduce((a, b) => Monoid.combine(a, b)(orResultMonoid))

  private def convertToComputable(token: Token): Option[Computable] = {
    token match
      case e: BasicToken.Expression => Some(() => Evaluator.evaluate(e.tokens))
      case c: BasicToken.Condition  => Some(() => c.rule.compute())
      case _ => None
  }

  private def splitBy[T](list: List[T], predicate: T => Boolean): List[List[T]] = {
    val indices = list.indices.filter(i => predicate(list(i)))

    def splitListByIndices(list: List[T], indices: List[Int]): List[List[T]] = {
      @tailrec
      def loop(list: List[T], indices: List[Int], result: List[List[T]]): List[List[T]] = {
        indices match {
          case Nil => result :+ list
          case index :: remainingIndices =>
            val (head, tail) = list.splitAt(index)
            loop(tail.tail, remainingIndices, result :+ head)
        }
      }

      if (indices.isEmpty) {
        List(list)
      } else {
        loop(list, indices.sorted, Nil)
      }
    }

    splitListByIndices(list, indices.toList)
  }

}