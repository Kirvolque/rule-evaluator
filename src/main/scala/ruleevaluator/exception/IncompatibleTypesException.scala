package ruleevaluator.exception

import ruleevaluator.token.Token

class IncompatibleTypesException(operator: Token[Any], argument1: Token[Any], argument2: Token[Any]) extends RuntimeException(
  s"Incompatible types: ${operator.tokenType} ($argument1, $argument2)"
)
