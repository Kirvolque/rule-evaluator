package ruleevaluator.token

import ruleevaluator.result.Result
import ruleevaluator.rule.{Computable, Rule}
import ruleevaluator.token.Token.CombinedToken

/**
 * A trait representing a token in a rule expression.
 */
sealed trait Token

/**
 * An enumeration of basic tokens that make up a rule expression.
 */
enum BasicToken extends Token:

  /**
   * A token representing a group of sub-tokens enclosed in brackets.
   * @param tokens the list of sub-tokens that make up the expression inside the brackets
   */
  case RawExpression(val tokens: List[Token])

  /**
   * A whitespace token.
   */
  case Whitespace

/**
 * An enumeration of logical operators used in rule expressions.
 */
enum LogicalOperator extends Token:

  /**
   * The logical "and" operator.
   */
  case And

  /**
   * The logical "or" operator.
   */
  case Or

/**
 * An enumeration of comparison operators used in rule expressions.
 */
enum ComparisonOperator extends Token:

  /**
   * The equality operator.
   */
  case Equal

  /**
   * The inequality operator.
   */
  case NotEqual

  /**
   * The greater than operator.
   */
  case Greater

  /**
   * The greater than or equal to operator.
   */
  case GreaterEqual

  /**
   * The less than operator.
   */
  case Less

  /**
   * The less than or equal to operator.
   */
  case LessEqual

/**
 * An enumeration of argument types used in rule expressions.
 */
enum Argument extends Token:

  /**
   * A CSV field argument.
   *
   * @param name  the name of the CSV field
   * @param value the value of the CSV field, which can be either a string or a double
   */
  case CsvField(val name: String, val value: String | Double)

  /**
   * A string argument.
   *
   * @param value the value of the string argument
   */
  case StringArg(val value: String)

  /**
   * A double argument.
   *
   * @param value the value of the double argument
   */
  case DoubleArg(val value: Double)

/**
 * An enumeration of composite tokens that represent more complex structures in a rule expression.
 */
enum Composite extends Token:
  /**
   * A condition token representing a rule.
   *
   * @param rule the rule that the condition represents
   */
  case Condition(val rule: Rule)

  /**
   * An expression token consisting of a list of sub-tokens.
   *
   * @param tokens the list of sub-tokens that make up the expression
   */
  case Expression(val tokens: List[CombinedToken])

object Token {

  /**
   * A type representing a token that combines multiple tokens into a more complex structure.
   */
  type CombinedToken = Composite | LogicalOperator

}
