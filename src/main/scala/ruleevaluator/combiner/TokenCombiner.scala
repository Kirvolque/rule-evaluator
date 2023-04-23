package ruleevaluator.combiner

import ruleevaluator.rule.Rule
import ruleevaluator.exception.{InvalidConditionException, MissingArgumentException}
import ruleevaluator.token.*
import ruleevaluator.token.BasicToken.Expression

/**
 * A TokenCombiner takes a list of tokens and combines them to form a list of conditions.
 *
 * @param tokens The list of tokens to combine
 * @param line   The line number of the tokens in the original source code
 */
class TokenCombiner(val tokens: List[Token], val line: Int) {

  /**
   * Combine the tokens to form a list of conditions.
   *
   * @return A list of conditions
   * @throws InvalidConditionException If an invalid condition is detected
   * @throws MissingArgumentException  If an argument is missing for an operator
   */
  def combineTokensToConditions(): List[Token] =
    ensureExpressionContainsMoreThanOneToken()
    val tokenIterator = TokenIterator(tokens)
    tokenIterator.map {
      case TokenFrame(Some(_), token: LogicalOperator, Some(_)) => token
      case TokenFrame(_, token: LogicalOperator, _) => throw MissingArgumentException(
        s"Missing Argument(s) for operator: $token in line: $line.")
      case TokenFrame(Some(a1: Argument), c: ComparisonOperator, Some(a2: Argument)) => BasicToken.Condition(Rule(c, a1, a2))
      case tokenFrame@TokenFrame(_, _: Argument, _) => validateOrderOfTokens(tokenFrame)
      case TokenFrame(_, e: Expression, _) => simplify(e)
      case tokenFrame => tokenFrame.currentToken
    }
      .toList
      .filterNot(token => token.isInstanceOf[Argument])

  private def simplify(expression: BasicToken.Expression): BasicToken.Expression =
    val tokens = new TokenCombiner(expression.tokens, line).combineTokensToConditions()
    BasicToken.Expression(tokens)

  private def ensureExpressionContainsMoreThanOneToken(): Unit =
    if tokens.size == 1 && !tokens.head.isInstanceOf[BasicToken.Expression] then
      throw InvalidConditionException(
        s"Invalid condition in line $line."
      )

  private def validateOrderOfTokens(tokenFrame: TokenFrame): Token = {
    def isArgumentOrExpression(t: Token) = t.isInstanceOf[Argument | Expression]

    val orderIsInvalid =
      tokenFrame.previousToken.exists(isArgumentOrExpression) ||
        tokenFrame.nextToken.exists(isArgumentOrExpression)

    if orderIsInvalid then
      throw InvalidConditionException(s"Operator is missing in line $line.")
    else
      tokenFrame.currentToken
  }

}
