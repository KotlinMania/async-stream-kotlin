// port-lint: source src/lib.rs
package io.github.kotlinmania.asyncstream

/**
 * Asynchronous fallible stream.
 *
 * See the module documentation in [Stream.kt] for more details.
 *
 * Each successful element produced inside the block is wrapped in
 * `Result.success(...)` by the caller and forwarded through [Sender.send].
 * Any [Throwable] that escapes the block is caught and re-emitted as a final
 * `Result.failure(t)` element, mirroring the upstream macro's behavior when
 * the block returns `Err(e)` via Rust's `?` propagation.
 *
 * # Examples
 *
 * ```kotlin
 * import io.github.kotlinmania.asyncstream.tryStream
 * import kotlinx.coroutines.flow.toList
 * import kotlinx.coroutines.runBlocking
 *
 * fun main() = runBlocking {
 *     val s = tryStream<String> {
 *         send(Result.success("hello"))
 *         throw RuntimeException("world")
 *     }
 *
 *     val values = s.toList()
 *     require(values.size == 2)
 *     require(values[0].getOrThrow() == "hello")
 *     require(values[1].isFailure)
 * }
 * ```
 */
fun <T> tryStream(block: suspend Sender<Result<T>>.() -> Unit): AsyncStream<Result<T>> {
    val (tx, rx) = pair<Result<T>>()
    return AsyncStream(rx) {
        try {
            tx.block()
        } catch (t: Throwable) {
            tx.send(Result.failure(t))
        }
    }
}
