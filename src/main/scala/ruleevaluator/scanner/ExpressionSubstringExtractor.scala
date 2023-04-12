package ruleevaluator.scanner

import ruleevaluator.exception.{CharacterNotFoundException, UnexpectedCharacterException}

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

class ExpressionSubstringExtractor(val source: String, val line: Int) {
  private val punctuationMarks = collection.mutable.Stack[Char]()
  private var current = 0
  
  def extractExpressionStringFrom(startPosition: Int): String = {
    current = startPosition
    advance()
    while (punctuationMarks.nonEmpty) {
      advance()
    }
    source.substring(startPosition + 1, current - 1)
  }

  private def advance(): Unit = {
    if (current >= source.length) {
      throw CharacterNotFoundException(s"Missing ')' in line $line")
    }
    val currentCharacter = source.charAt(current)
    currentCharacter match {
      case '(' => addToStack('(')
      case ')' => removeFromStack('(')
      case '"' => moveToNext('"')
      case '[' => moveToNext(']')
      case _ => current += 1
    }
  }

  private def moveToNext(character: Char): Boolean = {
    val index = source.indexOf(character, current)
    if (index < 0) {
      throw CharacterNotFoundException(s"Missing '$character' in line $line")
    }
    current = index + 1
    true
  }

  private def addToStack(character: Char): Boolean = {
    punctuationMarks.push(character)
    current += 1
    true
  }

  private def removeFromStack(character: Char): Boolean = {
    Try(punctuationMarks.pop()) match {
      case Success(top) if top == character =>
        current += 1
        true
      case Success(top) =>
        punctuationMarks.push(top)
        true
      case Failure(_) =>
        throw UnexpectedCharacterException(s"Unexpected character '$character' in line $line: $source")
    }
  }
}

