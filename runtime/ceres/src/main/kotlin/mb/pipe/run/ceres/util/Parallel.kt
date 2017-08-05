package mb.pipe.run.ceres.util

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class PIterable<out T>(val it: Iterable<T>, val executorService: ExecutorService) : Iterable<T> by it

fun <T> Iterable<T>.par(
  numThreads: Int = maxOf(Runtime.getRuntime().availableProcessors() - 1, 1),
  executorService: ExecutorService = Executors.newFixedThreadPool(numThreads)
): PIterable<T> {
  return PIterable(this, executorService)
}

fun <T> PIterable<T>.unpar(): Iterable<T> {
  return this.it
}

fun <T, R> PIterable<T>.map(transform: (T) -> R): PIterable<R> {
  val destination = ConcurrentLinkedQueue<R>()
  val futures = this.asIterable().map { executorService.submit { destination.add(transform(it)) } }
  futures.map { it.get() }
  return PIterable(destination, executorService)
}

fun <T> PIterable<T>.forEach(action: (T) -> Unit): Unit {
  val futures = this.asIterable().map { executorService.submit { action(it) } }
  futures.map { it.get() }
}
