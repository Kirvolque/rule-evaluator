package ruleevaluator.token

// TODO: Add type restrictions. Get rid of Any.
class Token[T](val tokenType: TokenType, val value: Option[T] = None) {

  def hasType(expected: TokenType): Boolean = tokenType == expected

  def isArgument: Boolean = TokenType.ARGUMENTS.contains(tokenType)

  def isLogicalOperator: Boolean = TokenType.LOGICAL_OPERATORS.contains(tokenType)

  def isComparisonOperator: Boolean = TokenType.COMPARISON_OPERATORS.contains(tokenType)

  def isExpression: Boolean = hasType(TokenType.EXPRESSION)

  override def toString: String = value match {
    case Some(v) => s"$tokenType: $v"
    case None => tokenType.toString
  }
}
