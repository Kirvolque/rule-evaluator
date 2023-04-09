package ruleevaluator.csv

class CsvField(val name: String, val value: String) {

  override def toString: String = s"$name: $value"

}