package ruleevaluator.evaluator

import ruleevaluator.rule.Computable
import ruleevaluator.result.ResultMonoid._
import ruleevaluator.token._
import ruleevaluator.result.Result
import ruleevaluator.token.Token.CombinedToken
import ruleevaluator.util.Util
import cats.implicits._

/**
 * The Evaluator object provides a single public method that takes a list of tokens representing rules 
 * (in tokenized form) and returns the result of their evaluation
 */
object Evaluator {

  /**
   * Evaluates a list of tokens representing a set of rules and returns the overall result.
   *
   * @param tokens The list of tokens representing the rules to be evaluated.
   * @return The `Result` of the evaluation of the given rules.
   */
  def evaluate(tokens: List[CombinedToken]): Result =
    Util.splitBy[CombinedToken](tokens, token => token.isInstanceOf[LogicalOperator.Or.type])
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
      case Composite.Expression(tokens) => Some(() => Evaluator.evaluate(tokens))
      case Composite.Condition(rule)    => Some(() => rule.compute())
      case _                             => None
  }

}
