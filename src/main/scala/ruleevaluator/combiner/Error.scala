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
 * @param line The line number where the invalid condition was detected
 */
case class InvalidCondition(line: Int) extends TokenError(s"Invalid condition in line $line.")

/**
 * A case class that represents an error for a missing argument for an operator during token combination.
 *
 * @param operator The logical operator for which an argument is missing
 * @param line The line number where the missing argument was detected
 */
case class MissingArgument(operator: LogicalOperator, line: Int)
  extends TokenError(s"Missing Argument(s) for operator: $operator in line: $line.")
