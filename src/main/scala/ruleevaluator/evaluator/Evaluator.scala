package ruleevaluator.evaluator

import ruleevaluator.rule.{Computable, Result}
import ruleevaluator.rule.ResultMonoid.*
import ruleevaluator.token.Argument.CsvField
import ruleevaluator.token.{Argument, BasicToken, ComparisonOperator, LogicalOperator, Token}
import cats.implicits.*
import ruleevaluator.util.Util


object Evaluator {
  def evaluate(tokens: List[Token]): Result =
    Util.splitBy[Token](tokens, token => token.isInstanceOf[LogicalOperator.Or.type])
      .map(list =>
        Util.splitBy(list, token => token.isInstanceOf[LogicalOperator.And.type])
          .flatten
          .flatMap(convertToComputable)
          .map(_.compute())
          .combineAll(andResultMonoid)
      )
      .combineAll(orResultMonoid)

  private def convertToComputable(token: Token): Option[Computable] = {
    token match
      case e: BasicToken.Expression => Some(() => Evaluator.evaluate(e.tokens))
      case c: BasicToken.Condition  => Some(() => c.rule.compute())
      case _ => None
  }

}