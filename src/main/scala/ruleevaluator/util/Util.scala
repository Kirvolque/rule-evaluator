package ruleevaluator.util

import scala.annotation.tailrec

/**
 * This object contains utility functions for common operations.
 */
object Util {

  /**
   * Splits a list into sublists using a given predicate as a splitting criterion.
   *
   * @param list      The list to split.
   * @param predicate The predicate to use as a splitting criterion.
   * @return A list of sublists.
   */
  def splitBy[T](list: List[T], predicate: T => Boolean): List[List[T]] = {
    // TODO: make this method more efficient
    val indices = list.indices.filter(i => predicate(list(i)))

    def splitListByIndices(list: List[T], indices: List[Int]): List[List[T]] = {
      @tailrec
      def loop(list: List[T], indices: List[Int], result: List[List[T]]): List[List[T]] = {
        indices match {
          case Nil => result :+ list
          case index :: remainingIndices =>
            val (head, tail) = list.splitAt(index)
            loop(tail.tail, remainingIndices, result :+ head)
        }
      }

      if (indices.isEmpty) {
        List(list)
      } else {
        loop(list, indices.sorted, Nil)
      }
    }

    splitListByIndices(list, indices.toList)
  }

}
