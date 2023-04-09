package ruleevaluator.rulesfile


class RulesFileContent(val lines: List[RuleLine]) {
  def isEmpty: Boolean =
    lines.forall(line => line.lineString.isBlank)

}
