package ruleevaluator.csv

import scala.collection.immutable.Map

/**
 * Represents a row in a CSV file.
 *
 * @param content A map containing the field names and their values for this row.
 */
class CsvRow(val content: Map[String, String]) {

  /**
   * Returns the value of the specified field in this row.
   *
   * @throws ruleevaluator.exception.NoSuchFieldException If the specified field does not exist in this row.
   * @param field The name of the field to retrieve the value for.
   * @return The value of the specified field.
   */
  def getField(field: String): String =
    content.get(field) match {
      case Some(value) => value.trim()
      case None => throw ruleevaluator.exception.NoSuchFieldException(s"CSV file doesn't have field $field.")
    }
}
