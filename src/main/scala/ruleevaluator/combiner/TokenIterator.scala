package ruleevaluator.combiner

import ruleevaluator.token.Token

class TokenIterator(val tokens: List[Token]) extends Iterator[TokenFrame] {

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
