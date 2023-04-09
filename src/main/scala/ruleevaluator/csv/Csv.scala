package ruleevaluator.csv

import scala.collection.immutable.Map

class Csv(val content: Map[String, String]) {

  def getField(field: String): String =
    content.get(field) match {
      case Some(value) => value.trim()
      case None => throw ruleevaluator.exception.NoSuchFieldException(s"CSV file doesn't have field $field.")
    }
}
