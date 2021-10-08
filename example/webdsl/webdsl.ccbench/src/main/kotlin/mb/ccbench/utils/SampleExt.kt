package mb.ccbench.utils

import java.util.*

/**
 * Samples a number of elements from this list
 * in a random order.
 *
 * If the number of samples is greater than the number of elements in the list,
 * the whole list is randomized.
 *
 * @param number the number of samples
 * @param rnd the random number generator
 */
fun <T> Iterable<T>.sample(number: Int, rnd: Random): List<T> {
    val input = this.toMutableList()
    val picked = mutableListOf<T>()
    for (i in 0 until number) {
        val pick = input.removeAt(rnd.nextInt(input.size))
        picked.add(pick)
        if (input.isEmpty()) break
    }
    return picked
}
