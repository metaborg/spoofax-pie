package mb.pipe.run.ceres.util

import mb.ceres.Out
import java.util.ArrayList

fun <T : Out> list(vararg elements: T): ArrayList<T> {
  val list = ArrayList<T>()
  list.addAll(elements)
  return list
}

fun <T : Out> ArrayList<T>.append(vararg elements: T): ArrayList<T> {
  addAll(elements)
  return this
}