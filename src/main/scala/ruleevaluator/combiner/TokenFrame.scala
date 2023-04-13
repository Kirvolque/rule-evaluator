package ruleevaluator.combiner

import ruleevaluator.token.Token

/**
 * Represents a frame of three consecutive tokens: the previous token, the current token, and the next token.
 *
 * @param previousToken an optional token that represents the token that appears before the current token
 * @param currentToken  the token in the middle of the frame
 * @param nextToken     an optional token that represents the token that appears after the current token
 */
case class TokenFrame(previousToken: Option[Token], currentToken: Token, nextToken: Option[Token])
