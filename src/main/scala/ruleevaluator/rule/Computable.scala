package ruleevaluator.rule

import ruleevaluator.rule.Result

trait Computable {
  def compute(): Result
}