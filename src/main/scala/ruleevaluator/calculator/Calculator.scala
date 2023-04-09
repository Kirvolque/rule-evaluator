package ruleevaluator.calculator

import ruleevaluator.rule.{Computable, Result}
import ruleevaluator.expression.Expression
import ruleevaluator.token.{Token, TokenType}

import scala.annotation.tailrec

object Calculator {
  def calculate(tokens: List[Token[Any]]): Result =
    splitBy[Token[Any]](tokens, token => token.hasType(TokenType.OR))
      .map(list =>
        splitBy(list, token => token.hasType(TokenType.AND))
          .flatten
          .map(convertToCalculable)
          .map(calculable => calculable.asInstanceOf[Computable].compute())
          .fold(Result.PASS)((a, b) => a combineWithAnd b)
      )
      .reduce(_.combineWithOr(_))

  private def convertToCalculable(token: Token[Any]): Computable =
    if (token.hasType(TokenType.EXPRESSION)) expressionToCalculable(token)
    else token.value.get.asInstanceOf[Computable] // TODO remove get

  private def expressionToCalculable(token: Token[Any]): Computable = {
    val expression = token.value.get.asInstanceOf[Expression] // TODO remove get
    () => Calculator.calculate(expression.tokens)
  }

  /** Splits the given list into sub-lists based on the given predicate.
   * The elements matching the predicate are used as borders, but not included in the resulting sub-lists.
   *
   * @param list      the list to split
   * @param predicate the predicate to match against elements
   * @tparam T the type of elements in the list
   * @return a list of sub-lists
   */
  def splitBy[T](list: List[T], predicate: T => Boolean): List[List[T]] = {
    val indices = list.indices.filter(i => predicate(list(i)))

    def splitListByIndices[T](list: List[T], indices: List[Int]): List[List[T]] = {
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
