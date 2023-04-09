package ruleevaluator.rule

import ruleevaluator.exception.{IncompatibleTypesException, WrongTypeException}
import ruleevaluator.token.{Token, TokenType}
import ruleevaluator.csv.{Csv, CsvField}
import ruleevaluator.rule.{Result, Computable}

class Rule(operator: Token[Any], argument1: Token[Any], argument2: Token[Any]) extends Computable {
  override def compute(): Result =
    operator.tokenType match {
      case TokenType.LESS => check((arg1, arg2) => arg1 < arg2)
      case TokenType.LESS_EQUAL => check((arg1, arg2) => arg1 <= arg2)
      case TokenType.GREATER => check((arg1, arg2) => arg1 > arg2)
      case TokenType.GREATER_EQUAL => check((arg1, arg2) => arg1 >= arg2)
      case TokenType.NOT_EQUAL => notEqual()
      case TokenType.EQUAL => equal()
      case _ => throw new MatchError(operator.tokenType)
    }

  private def notEqual(): Result = negateResult(equal())

  private def equal(): Result = {
    if (oneOfArgumentsHasType(TokenType.STRING)) {
      stringEquals()
    } else if (oneOfArgumentsHasType(TokenType.DOUBLE)) {
      check((a, b) => a == b)
    } else if (bothArgumentsHaveType(TokenType.CSV_FIELD)) {
      convertFieldValueToDouble(argument1) match {
        case Some(_) => check((a, b) => a == b)
        case None => stringEquals()
      }
    } else throw IncompatibleTypesException(operator, argument1, argument2)
  }

  private def negateResult(result: Result): Result = if (result.successful) Result.fail(Seq(argument1, argument2)) else Result.PASS

  private def stringEquals(): Result = if (getString(argument1) == getString(argument2)) Result.PASS else Result.fail(Seq(argument1, argument2))

  private def oneOfArgumentsHasType(tokenType: TokenType): Boolean = argument1.hasType(tokenType) || argument2.hasType(tokenType)

  private def bothArgumentsHaveType(tokenType: TokenType): Boolean = argument1.hasType(tokenType) && argument2.hasType(tokenType)

  private def check(predicate: (Double, Double) => Boolean): Result = {
    val arg1 = getDouble(argument1)
    val arg2 = getDouble(argument2)
    if arg1.isEmpty || arg2.isEmpty then Result.fail(Seq(argument1, argument2))
    else checkPredicate(predicate, arg1.get, arg2.get)
  }

  private def checkPredicate[T](predicate: (T, T) => Boolean, arg1: T, arg2: T): Result = {
    if (predicate(arg1, arg2)) Result.PASS else Result.fail(Seq(argument1, argument2))
  }

  private def getDouble(argument: Token[Any]): Option[Double] = argument.tokenType match {
    case TokenType.CSV_FIELD => convertFieldValueToDouble(argument)
    case TokenType.DOUBLE => Some(argument.value.get.asInstanceOf[Double]) // TODO remove get
    case _ => throw WrongTypeException(s"Double expected number but found ${argument.toString}.")
  }

  private def getString(argument: Token[Any]): String = argument.tokenType match {
    case TokenType.CSV_FIELD => getFieldValue(argument)
    case TokenType.STRING => argument.value.get.asInstanceOf[String] // TODO remove get
    case _ => throw WrongTypeException(s"String expected number but found ${argument.toString}.")
  }

  private def convertFieldValueToDouble(argument: Token[Any]): Option[Double] = {
    val field = argument.value.get.asInstanceOf[CsvField] // TODO remove get
    try {
      Some(field.value.toDouble)
    } catch {
      case _: NumberFormatException => None
    }
  }

  private def getFieldValue(argument: Token[Any]): String = {
    val csvField = argument.value.get.asInstanceOf[CsvField] // TODO remove get
    csvField.value
  }
}

