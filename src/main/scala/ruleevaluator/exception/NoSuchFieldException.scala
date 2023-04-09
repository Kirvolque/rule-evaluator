package ruleevaluator.exception

class NoSuchFieldException(fieldName: String) extends RuntimeException(
  s"CSV file doesn't have field $fieldName."
)
