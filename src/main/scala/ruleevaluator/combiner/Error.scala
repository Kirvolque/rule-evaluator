package ruleevaluator.combiner

import ruleevaluator.token.LogicalOperator

/**
 * A trait that represents an error that can occur during token combination.
 */
sealed trait Error

/**
 * A class that represents a token combination error with a message.
 *
 * @param message The error message
 */
class TokenError(val message: String) extends Error

/**
 * A case class that represents an error for an invalid condition detected during token combination.
 *
 * @param message The error message
 */
case class InvalidCondition(line: Int) extends TokenError(s"Invalid condition in line $line.")

/**
 * A case class that represents an error for a missing argument for an operator during token combination.
 *
 * @param message The error message
 */
case class MissingArgument(operator: LogicalOperator, line: Int) 
  extends TokenError(s"Missing Argument(s) for operator: $operator in line: $line.")
