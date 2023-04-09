package ruleevaluator.scanner

import ruleevaluator.rulesfile.RuleLine
import ruleevaluator.exception.{CharacterNotFoundException, UnexpectedCharacterException}
import ruleevaluator.expression.Expression
import ruleevaluator.csv.{Csv, CsvField}
import ruleevaluator.token.{Token, TokenType}
import ruleevaluator.scanner.ExpressionSubstringExtractor

class Scanner(val source: String,
              val csv: Csv,
              val line: Int) extends Iterator[Token[Any]] {

  private var current: Int = 0

  private val OR: String = "or"
  private val AND: String = "and"
  private val NOT_EQUAL: String = "!="

  private val tokenLexemeMap: Map[TokenType, String] = Map(
    TokenType.AND -> AND,
    TokenType.OR -> OR,
    TokenType.NOT_EQUAL -> NOT_EQUAL
  )
  def this(conditionLine: RuleLine, csv: Csv) =
    this(conditionLine.lineString, csv, conditionLine.lineNumber)

  override def hasNext: Boolean = current < source.length()

  override def next(): Token[Any] = {
    val c = advance()
    c match {
      case '(' => parseExpression()
      case '"' => new Token(TokenType.STRING, Some(readUntilNext('"')))
      case '[' => new Token(TokenType.CSV_FIELD, Some(getcsvFieldValue))
      case '=' => new Token(TokenType.EQUAL)
      case '<' => new Token(if (isFollowedBy('=')) TokenType.LESS_EQUAL else TokenType.LESS)
      case '>' => new Token(if (isFollowedBy('=')) TokenType.GREATER_EQUAL else TokenType.GREATER)
      case ch: Char if isDigit(ch) => parseNumber()
      case _ if isLexeme(NOT_EQUAL) => read(TokenType.NOT_EQUAL)
      case _ if isLexeme(OR) => read(TokenType.OR)
      case _ if isLexeme(AND) => read(TokenType.AND)
      case ' ' => new Token(TokenType.WHITESPACE)
      case _ => throw new UnexpectedCharacterException(
        s"Unexpected character '$c' in line $line: $source."
      )
    }
  }

  def parseTokens(): List[Token[Any]] = {
    this.iterator.filter(!_.hasType(TokenType.WHITESPACE)).toList
  }

  private def advance(): Char = {
    current += 1
    source.charAt(current - 1)
  }

  private def isFollowedBy(expected: Char): Boolean = {
    if (!hasNext) false
    else if (source.charAt(current) != expected) false
    else {
      current += 1
      true
    }
  }

  private def read(tokenType: TokenType): Token[Any] = {
    current += tokenLexemeMap.get(tokenType).size + 1
    new Token(tokenType)
  }

  private def parseExpression(): Token[Any] = {
    val expressionReader = ExpressionSubstringExtractor(source, line)
    val expressionString = expressionReader.extractExpressionStringFrom(current - 1)
    current += expressionString.length + 1
    val scanner = new Scanner(expressionString, csv, line)
    new Token(TokenType.EXPRESSION, Some(Expression(scanner.parseTokens())))
  }

  private def getcsvFieldValue: CsvField = {
    val name = readUntilNext(']')
    new CsvField(name, csv.getField(name))
  }

  private def readUntilNext(character: Char): String = {
    val index = findNext(character)
    val substring = source.substring(current, index)
    current = index + 1
    substring.trim
  }

  private def findNext(character: Char): Int = {
    val index = source.indexOf(character, current)
    if (index < 0) {
      throw CharacterNotFoundException(s"Missing '$character' in line $line")
    }
    index
  }

  private def isLexeme(lexeme: String): Boolean = {
    val lastIndex = current + lexeme.length - 1
    lastIndex <= source.length &&
      lexeme.equalsIgnoreCase(source.substring(current - 1, lastIndex))
  }

  private def isDigit(character: Char): Boolean =
    Character.isDigit(character) || character == '-'

  private def parseNumber(): Token[Any] = {
    val lastIndex = (current until source.length)
      .filterNot(i => isDigit(source.charAt(i)) || source.charAt(i) == '.')
      .headOption
      .getOrElse(source.length)
    val number = source.substring(current - 1, lastIndex)
    current = lastIndex + 1
    Token(TokenType.DOUBLE, Some(number.toDouble))
  }

}
