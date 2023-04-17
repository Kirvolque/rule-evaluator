package ruleevaluator.rulesfile

/**
 * A class that represents the content of a rules file.
 *
 * @param lines a list of RuleLine instances representing each line in the rules file
 */
class RulesFileContent(val lines: List[RuleLine]) {

  /**
   * Checks whether the rules file is empty, i.e., all lines in the file are blank.
   *
   * @return true if the rules file is empty, false otherwise
   */
  def isEmpty: Boolean =
    lines.forall(line => line.lineString.isBlank)

}
