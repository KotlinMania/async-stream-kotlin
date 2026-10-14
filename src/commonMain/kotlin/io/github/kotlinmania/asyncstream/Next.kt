// port-lint: source next.rs
package io.github.kotlinmania.asyncstream

import io.github.kotlinmania.futures.Poll
import io.github.kotlinmania.futures.Stream
import io.github.kotlinmania.futures.TaskContext
import io.github.kotlinmania.futures.Yield
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

/**
 * Future that pulls the next item from a stream.
 */
internal class Next<T>(
    private val stream: Stream<T>,
) {
    fun poll(context: TaskContext): Poll<T?> =
        when (val y = stream.pollNext(context)) {
            is Poll.Ready ->
                when (val v = y.value) {
                    is Yield.Value -> Poll.Ready(v.value)
                    Yield.End -> Poll.Ready(null)
                }
            Poll.Pending -> Poll.Pending
        }
}

/**
 * Returns a future that pulls the next item from `stream`.
 */
internal fun <T> next(stream: Stream<T>): Next<T> = Next(stream)

/**
 * Pull the next item from `stream`, or `null` if the stream has terminated.
 *
 * Upstream's helper is generic over any unpinnable stream type and returns a
 * future that mutably borrows the stream and polls it exactly once. The Kotlin
 * translation accepts any [Flow] and reads its first emission. Type parameter
 * `T` is constrained to [Any] because [firstOrNull] uses `null` as the
 * end-of-stream sentinel and would otherwise collide with a legitimate `null`
 * value.
 */
internal suspend fun <T : Any> next(stream: Flow<T>): T? = stream.firstOrNull()
