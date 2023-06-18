package ruleevaluator.combiner

import ruleevaluator.token.Token

/**
 * A class that provides an iterator to traverse a list of tokens in a frame of three consecutive tokens:
 * the previous token, the current token, and the next token.
 *
 * @param tokens a list of tokens to be iterated over
 */
private[combiner] class TokenIterator(val tokens: List[Token]) extends Iterator[TokenFrame] {

  private var current = 0

  override def hasNext(): Boolean = current < tokens.size

  override def next(): TokenFrame =
    current += 1
    TokenFrame(getPrevious, tokens(current - 1), getNext)

  private def getPrevious: Option[Token] =
    if current <= 1 then None else Some(tokens(current - 2))

  private def getNext: Option[Token] =
    if !hasNext() then None else Some(tokens(current))

}
