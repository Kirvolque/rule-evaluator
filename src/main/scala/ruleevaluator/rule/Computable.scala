package ruleevaluator.rule

import ruleevaluator.rule.Result

/**
 * A trait representing something that can be computed to produce a result.
 * Implementations of this trait must provide a compute method that returns a Result.
 */
trait Computable {

  /**
   * Computes and returns the result of this Computable.
   * @return the result of this Computable.
   */
  def compute(): Result
}