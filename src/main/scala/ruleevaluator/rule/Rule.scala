package ruleevaluator.rule

import ruleevaluator.exception.{IncompatibleTypesException, WrongTypeException}
import ruleevaluator.token.{Argument, ComparisonOperator, Token}
import ruleevaluator.csv.Csv
import ruleevaluator.exception
import ruleevaluator.rule.{Computable, Result}

/**
 * Represents a rule that compares two arguments using a comparison operator.
 *
 * @param operator  the comparison operator used in the rule
 * @param argument1 the first argument used in the comparison
 * @param argument2 the second argument used in the comparison
 */
case class Rule(operator: ComparisonOperator, argument1: Argument, argument2: Argument) extends Computable {

  /**
   * Computes the result of the rule by applying the specified comparison operator to the two arguments.
   *
   * @return a Result object representing the outcome of the comparison
   * @throws IncompatibleTypesException if the two arguments are not compatible for the specified comparison operator
   * @throws WrongTypeException         if either argument is not a valid type for the specified comparison operator
   */
  override def compute(): Result = {
    operator match {
      case ComparisonOperator.Less         => check((arg1, arg2) => arg1 < arg2)
      case ComparisonOperator.LessEqual    => check((arg1, arg2) => arg1 <= arg2)
      case ComparisonOperator.Greater      => check((arg1, arg2) => arg1 > arg2)
      case ComparisonOperator.GreaterEqual => check((arg1, arg2) => arg1 >= arg2)
      case ComparisonOperator.NotEqual     => notEqual()
      case ComparisonOperator.Equal        => equal()
    }
  }

  private def notEqual(): Result = negateResult(equal())

  private def equal(): Result = (argument1, argument2) match
    case (_: Argument.StringArg, _) => stringEquals()
    case (_, _: Argument.StringArg) => stringEquals()
    case (_: Argument.DoubleArg, _) => check((a, b) => a == b)
    case (_, _: Argument.DoubleArg) => check((a, b) => a == b)
    case (a1: Argument.CsvField, _: Argument.CsvField) =>
      convertFieldValueToDouble(a1) match
        case Some(_) => check((a, b) => a == b)
        case None => stringEquals()
    case _ => throw IncompatibleTypesException(operator, argument1, argument2)

  private def negateResult(result: Result): Result = if (result.successful) Result.fail(Seq(argument1, argument2)) else Result.PASS

  private def stringEquals(): Result = if (getString(argument1) == getString(argument2)) Result.PASS else Result.fail(Seq(argument1, argument2))

  private def check(predicate: (Double, Double) => Boolean): Result = {
    val arg1 = getDouble(argument1)
    val arg2 = getDouble(argument2)
    if arg1.isEmpty || arg2.isEmpty then Result.fail(Seq(argument1, argument2))
    else checkPredicate(predicate, arg1.get, arg2.get)
  }

  private def checkPredicate[T](predicate: (T, T) => Boolean, arg1: T, arg2: T): Result = {
    if (predicate(arg1, arg2)) Result.PASS else Result.fail(Seq(argument1, argument2))
  }

  private def getDouble(argument: Argument): Option[Double] = argument match {
    case a: Argument.CsvField => convertFieldValueToDouble(a)
    case a: Argument.DoubleArg => Some(a.value)
    case _ => throw WrongTypeException(s"Double expected number but found ${argument.toString}.")
  }

  private def getString(argument: Argument): String = argument match {
    case f: Argument.CsvField => f.value.toString
    case s: Argument.StringArg => s.value
    case _ => throw WrongTypeException(s"String expected number but found ${argument.toString}.")
  }

  private def convertFieldValueToDouble(field: Argument.CsvField): Option[Double] = {
    try {
      Some(field.value.toString.toDouble)
    } catch {
      case _: NumberFormatException => None
    }
  }

}

