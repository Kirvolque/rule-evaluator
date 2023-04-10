package ruleevaluator.token

import ruleevaluator.rule.Rule

sealed trait Token

enum BasicToken extends Token:
  case Expression(val tokens: List[Token])
  case Condition(val rule: Rule)
  case Whitespace

enum LogicalOperator extends Token:
  case And
  case Or

enum ComparisonOperator extends Token:
  case Equal
  case NotEqual
  case Greater
  case GreaterEqual
  case Less
  case LessEqual

enum Argument extends Token:
  case CsvField(val name: String, val value: String | Double)
  case StringArg(val value: String)
  case DoubleArg(val value: Double)

