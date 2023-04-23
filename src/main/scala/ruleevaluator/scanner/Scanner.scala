package ruleevaluator.scanner

import ruleevaluator.exception.{CharacterNotFoundException, UnexpectedCharacterException}
import ruleevaluator.csv.CsvRow
import ruleevaluator.rule.RuleLine
import ruleevaluator.token.{Argument, BasicToken, ComparisonOperator, LogicalOperator, Token}
import ruleevaluator.scanner.ExpressionSubstringExtractor
import ruleevaluator.token.BasicToken.Whitespace

/** The Scanner class is responsible for tokenizing a given expression string.
 *
 *
 *
 * @constructor Create a new Scanner instance.
 * @param source The expression string to be tokenized.
 * @param csv    The Csv instance used to retrieve CSV field values.
 * @param line   The line number of the expression in the rule file.
 */
class Scanner(val source: String,
              val csv: CsvRow,
              val line: Int) extends Iterator[Token] {
  private var current: Int = 0

  private val OR: String = "or"
  private val AND: String = "and"
  private val NOT_EQUAL: String = "!="

  private val tokenLexemeMap: Map[Token, String] = Map(
    LogicalOperator.And -> AND,
    LogicalOperator.Or -> OR,
    ComparisonOperator.NotEqual -> NOT_EQUAL
  )

  /** Create a new Scanner instance.
   *
   * @param conditionLine The RuleLine instance containing the expression string to be tokenized and its line number.
   * @param csv           The Csv instance used to retrieve CSV field values.
   * @return A new Scanner instance.
   */
  def this(conditionLine: RuleLine, csv: CsvRow) =
    this(conditionLine.lineString, csv, conditionLine.lineNumber)

  /** Check if there are more tokens to be read.
   *
   * @return true if there are more tokens to be read, false otherwise.
   */
  override def hasNext: Boolean = current < source.length()

  /** Get the next token in the expression string.
   *
   * @return The next Token instance.
   */
  override def next(): Token = {
    val c = advance()
    c match {
      case '(' => parseExpression
      case '"' => Argument.StringArg(readUntilNext('"'))
      case '[' => getCsvFieldValue
      case '=' => ComparisonOperator.Equal
      case '<' => if (isFollowedBy('=')) ComparisonOperator.LessEqual    else ComparisonOperator.Less
      case '>' => if (isFollowedBy('=')) ComparisonOperator.GreaterEqual else ComparisonOperator.Greater
      case ch: Char if isDigit(ch)  => readNumber()
      case _ if isLexeme(NOT_EQUAL) => read(ComparisonOperator.NotEqual)
      case _ if isLexeme(OR)        => read(LogicalOperator.Or)
      case _ if isLexeme(AND)       => read(LogicalOperator.And)
      case ' ' => BasicToken.Whitespace
      case _ => throw new UnexpectedCharacterException(
        s"Unexpected character '$c' in line $line: $source."
      )
    }
  }

  /** Parse all the tokens in the expression string.
   *
   * @return A list of Token instances.
   */
  def parseTokens(): List[Token] = {
    this.filterNot(token => token.isInstanceOf[BasicToken.Whitespace.type]).toList
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

  private def read(token: Token): Token = {
    current += tokenLexemeMap.get(token).size + 1
    token
  }

  private def parseExpression: BasicToken.Expression = {
    val expressionReader = ExpressionSubstringExtractor(source, line)
    val expressionString = expressionReader.extractExpressionStringFrom(current - 1)
    current += expressionString.length + 1
    val scanner = new Scanner(expressionString, csv, line)
    BasicToken.Expression(scanner.parseTokens())
  }

  private def getCsvFieldValue: Argument.CsvField = {
    val name = readUntilNext(']')
    Argument.CsvField(name, csv.getField(name))
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

  private def readNumber(): Argument.DoubleArg = {
    val lastIndex = (current until source.length)
      .filterNot(i => isDigit(source.charAt(i)) || source.charAt(i) == '.')
      .headOption
      .getOrElse(source.length)
    val number = source.substring(current - 1, lastIndex)
    current = lastIndex + 1
    Argument.DoubleArg(number.toDouble)
  }

}
