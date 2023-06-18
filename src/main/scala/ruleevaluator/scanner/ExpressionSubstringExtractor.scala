package ruleevaluator.scanner

import ruleevaluator.exception.{CharacterNotFoundException, UnexpectedCharacterException}

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

/**
 *
 * A class to extract a substring representing an expression from a given source string.
 *
 * @constructor Create a new ExpressionSubstringExtractor with the specified source string and line number.
 * @param source the source string to extract the expression from
 * @param line   the line number in the source string where the expression starts
 */
private[scanner] class ExpressionSubstringExtractor(val source: String, val line: Int) {
  private val punctuationMarks = collection.mutable.Stack[Char]()
  private var current = 0

  /**
   * Extracts a substring representing an expression from the source string starting at the specified position.
   * This method advances the cursor through the source string and keeps track of punctuation marks and
   * string literals in order to determine where the expression ends.
   *
   * @param startPosition the starting position of the expression in the source string
   * @return the substring representing the expression
   * @throws CharacterNotFoundException   if a required character is not found in the source string
   * @throws UnexpectedCharacterException if an unexpected character is found in the source string
   */
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
    if (punctuationMarks.isEmpty || punctuationMarks.pop() != character) {
      throw UnexpectedCharacterException(s"Unexpected character '$character' in line $line: $source")
    }
    current += 1
    true
  }
}

