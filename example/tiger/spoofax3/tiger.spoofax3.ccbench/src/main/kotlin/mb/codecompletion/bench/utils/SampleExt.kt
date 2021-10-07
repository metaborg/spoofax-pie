package mb.codecompletion.bench.utils

import java.util.*

/**
 * Samples a number of elements from this list.
 *
 * @param number the number of samples
 * @param rnd the random number generator
 */
fun <T> Iterable<T>.sample(number: Int, rnd: Random): List<T> {
    val input = this.toMutableList()
    if (number >= input.size) return input
    val picked = mutableListOf<T>()
    for (i in 0 until number) {
        val pick = input.removeAt(rnd.nextInt(input.size))
        picked.add(pick)
    }
    return picked
}
