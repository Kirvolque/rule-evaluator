package ruleevaluator.util

import scala.annotation.tailrec

object Util {

  def splitBy[T](list: List[T], predicate: T => Boolean): List[List[T]] = {
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
