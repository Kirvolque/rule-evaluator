package ruleevaluator.exception

import ruleevaluator.combiner.TokenError

class InvalidRuleSyntaxException(errors: Iterable[TokenError])
  extends RuntimeException(
    errors
      .map(_.message)
      .mkString("\n")
  )
