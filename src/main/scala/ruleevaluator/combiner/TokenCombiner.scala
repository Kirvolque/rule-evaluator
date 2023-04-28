package ruleevaluator.combiner

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.implicits._
import ruleevaluator.rule.Rule
import ruleevaluator.exception.InvalidRuleSyntaxException
import ruleevaluator.token._
import ruleevaluator.combiner.{InvalidCondition, MissingArgument, TokenError}
import ruleevaluator.token.Token.ConditionToken

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
   * @return A validated list of tokens representing the conditions
   *         If the combination fails, a list of token errors is returned
   */
  def combineTokensToConditions(): Validated[List[TokenError], List[ConditionToken]] =
    val tokenIterator = TokenIterator(tokens)
    tokenIterator.map {
      case TokenFrame(None, _: Argument, None) => Invalid(List(InvalidCondition(s"Invalid condition in line $line.")))
      case TokenFrame(Some(_), token: LogicalOperator, Some(_)) => Valid(token)
      case TokenFrame(_, token: LogicalOperator, _) => Invalid(List(MissingArgument(
        s"Missing Argument(s) for operator: $token in line: $line.")))
      case TokenFrame(Some(a1: Argument), c: ComparisonOperator, Some(a2: Argument)) => Valid(Composite.Condition(Rule(c, a1, a2)))
      case tokenFrame@TokenFrame(_, _: Argument, _) => validateOrderOfTokens(tokenFrame)
      case TokenFrame(_, e: BasicToken.RawExpression, _) => constructExpression(e)
      case tokenFrame => Valid(tokenFrame.currentToken)
    }
      .toList
      .collect {
        case Valid(ct: ConditionToken) => Valid(List(ct))
        case i: Invalid[List[TokenError]] => i
      }
      .reduce((x, y) => x.combine(y))


  private def constructExpression(expression: BasicToken.RawExpression): Validated[List[TokenError], Composite.Expression] =
    val tokens = new TokenCombiner(expression.tokens, line).combineTokensToConditions()
    tokens.map(t => Composite.Expression(t))

  private def validateOrderOfTokens(tokenFrame: TokenFrame): Validated[List[TokenError], Token] = {
    def isArgumentOrExpression(t: Token) = t.isInstanceOf[Argument | BasicToken.RawExpression]

    val orderIsInvalid =
      tokenFrame.previousToken.exists(isArgumentOrExpression) ||
        tokenFrame.nextToken.exists(isArgumentOrExpression)

    if orderIsInvalid then
      Invalid(List(InvalidCondition(s"Operator is missing in line $line.")))
    else
      Valid(tokenFrame.currentToken)
  }

}
