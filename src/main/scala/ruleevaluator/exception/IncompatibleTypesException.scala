package ruleevaluator.exception

import ruleevaluator.token.{Argument, ComparisonOperator, LogicalOperator}

class IncompatibleTypesException(operator: ComparisonOperator, argument1: Argument, argument2: Argument) extends RuntimeException(
  s"Incompatible types: ${operator} ($argument1, $argument2)"
)
