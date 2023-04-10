package ruleevaluator.combiner

import ruleevaluator.rule.Rule
import ruleevaluator.exception.{InvalidConditionException, MissingArgumentException}
import ruleevaluator.token.*
import ruleevaluator.token.BasicToken.Expression

class TokenCombiner(val tokens: List[Token], val line: Int) extends Iterator[Token] {
  private var current = 0

  override def hasNext(): Boolean = current < tokens.size

  override def next(): Token =
    current += 1
    tokens(current - 1)

  def combineTokensToConditions(): List[Token] =
    ensureExpressionContainsMoreThanOneToken()
    this.map {
      case lo: LogicalOperator => ensureArgumentsAreGiven(lo)
      case a: Argument => validateOrderOfTokens(a)
      case _: ComparisonOperator => createConditionToken()
      case e: Expression => simplify(e)
      case token => token
    }
      .toList
      .filterNot(token => token.isInstanceOf[Argument])

  private def createConditionToken(): BasicToken.Condition =
    val rule = Rule(
      argument1 = getPreviousToken.asInstanceOf[Argument],
      operator = getCurrentToken.asInstanceOf[ComparisonOperator],
      argument2 = getNextToken.asInstanceOf[Argument])
    BasicToken.Condition(rule)

  private def simplify(expression: BasicToken.Expression): BasicToken.Expression =
    val tokens = new TokenCombiner(expression.tokens, line).combineTokensToConditions()
    BasicToken.Expression(tokens)

  private def ensureExpressionContainsMoreThanOneToken(): Unit =
    if tokens.size == 1 && tokens.head.isInstanceOf[BasicToken.Expression.type] then
      throw InvalidConditionException(
        s"Invalid condition in line $line."
      )

  private def validateOrderOfTokens(token: Token): Token = {
    def isArgumentOrExpression(t: Token) =
      t.isInstanceOf[Argument] || t.isInstanceOf[Expression]

    val orderIsInvalid =
      getPreviousOpt.exists(isArgumentOrExpression) ||
        getNextOpt.exists(isArgumentOrExpression)
    if orderIsInvalid then
      throw InvalidConditionException(
        s"Operator is missing in line $line."
      )
    else
      token
  }

  private def ensureArgumentsAreGiven(token: Token): Token =
    getPreviousToken
    getNextToken
    token

  private def getCurrentToken: Token =
    tokens(current - 1)

  private def getPreviousToken: Token =
    getPreviousOpt.getOrElse(
      throw MissingArgumentException(
        s"1st argument is missing for operator: ${tokens(current)} in line: $line."
      )
    )

  private def getNextToken: Token =
    getNextOpt.getOrElse(
      throw MissingArgumentException(
        s"2nd argument is missing for operator: ${tokens(current - 1)} in line: $line."
      )
    )

  private def getPreviousOpt: Option[Token] =
    if current <= 1 then None else Some(tokens(current - 2))

  private def getNextOpt: Option[Token] =
    if !hasNext() then None else Some(tokens(current))

}
