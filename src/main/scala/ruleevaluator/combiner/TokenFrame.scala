package ruleevaluator.combiner

import ruleevaluator.token.Token

case class TokenFrame(previousToken: Option[Token], currentToken: Token, nextToken: Option[Token])
