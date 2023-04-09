package ruleevaluator.combiner

import ruleevaluator.rule.Rule
import ruleevaluator.exception.{InvalidConditionException, MissingArgumentException}
import ruleevaluator.expression.Expression
import ruleevaluator.token.{Token, TokenType}

class TokenCombiner(val tokens: List[Token[Any]], val line: Int) extends Iterator[Token[Any]] {
  private var current = 0

  override def hasNext(): Boolean = current < tokens.size

  override def next(): Token[Any] =
    current += 1
    tokens(current - 1)

  // TODO rewrite using Validated
  def combineTokensToConditions(): List[Token[Any]] =
    ensureExpressionContainsMoreThanOneToken()
    this.map(token =>
      if token.isLogicalOperator then ensureArgumentsAreGiven(token)
      else token
    )
      .map(token =>
        if token.isArgument then validateOrderOfTokens(token)
        else token
      )
      .map(token =>
        if token.isComparisonOperator then createConditionToken()
        else token
      )
      .map(token =>
        if token.hasType(TokenType.EXPRESSION) then combineExpressionTokens(token)
        else token
      )
      .toList
      .filterNot(_.isArgument)

  private def createConditionToken(): Token[Any] =
    val condition = Rule(
      argument1 = getPreviousToken(),
      operator = getCurrentToken(),
      argument2 = getNextToken())
    new Token(TokenType.CONDITION, Some(condition))

  private def combineExpressionTokens(token: Token[Any]): Token[Any] =
    val expression = token.value.get.asInstanceOf[Expression] // TODO remove get
    val tokens = new TokenCombiner(expression.tokens, line).combineTokensToConditions()
    new Token(TokenType.EXPRESSION, Some(Expression(tokens)))

  private def ensureExpressionContainsMoreThanOneToken(): Unit =
    if tokens.size == 1 && !tokens.head.isExpression then
      throw InvalidConditionException(
        s"Invalid condition in line $line."
      )

  private def validateOrderOfTokens(token: Token[Any]): Token[Any] =
    val orderIsInvalid =
      getPrevious().exists(t => t.isArgument || t.isExpression) ||
        getNext().exists(t => t.isArgument || t.isExpression)
    if orderIsInvalid then
      throw InvalidConditionException(
        s"Operator is missing in line $line."
      )
    else
      token

  private def ensureArgumentsAreGiven(token: Token[Any]): Token[Any] =
    getPreviousToken()
    getNextToken()
    token

  private def getCurrentToken(): Token[Any] =
    tokens(current - 1)

  private def getPreviousToken(): Token[Any] =
    getPrevious().getOrElse(
      throw MissingArgumentException(
        s"1st argument is missing for operator: ${tokens(current)} in line: $line."
      )
    )

  private def getNextToken(): Token[Any] =
    getNext().getOrElse(
      throw MissingArgumentException(
        s"2nd argument is missing for operator: ${tokens(current - 1)} in line: $line."
      )
    )

  private def getPrevious(): Option[Token[Any]] =
    if current <= 1 then None else Some(tokens(current - 2))

  private def getNext(): Option[Token[Any]] =
    if !hasNext() then None else Some(tokens(current))

}
