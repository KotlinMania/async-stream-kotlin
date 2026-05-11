// port-lint: source src/lib.rs
package io.github.kotlinmania.asyncstream

/**
 * Asynchronous stream of elements.
 *
 * Provides two builders, [stream] and [tryStream], allowing the caller to
 * define asynchronous streams of elements. These are implemented using
 * suspend functions. This module works without unstable Kotlin features.
 *
 * The [stream] builder returns an anonymous type implementing the [Flow]
 * interface. The element type is the type of the values yielded from the
 * stream. The [tryStream] builder also returns an anonymous type implementing
 * the [Flow] interface, but the element type is `Result<T>`. The [tryStream]
 * builder supports using thrown exceptions as the equivalent of Rust's `?`
 * notation: any exception thrown inside the block is caught and forwarded as
 * the final `Result.failure` element.
 *
 * # Usage
 *
 * A basic stream yielding numbers. Values are yielded by calling `send`
 * inside the block. The block must return `Unit`.
 *
 * ```kotlin
 * import io.github.kotlinmania.asyncstream.stream
 * import kotlinx.coroutines.flow.collect
 * import kotlinx.coroutines.runBlocking
 *
 * fun main() = runBlocking {
 *     val s = stream<Int> {
 *         for (i in 0 until 3) {
 *             send(i)
 *         }
 *     }
 *
 *     s.collect { value ->
 *         println("got $value")
 *     }
 * }
 * ```
 *
 * Streams may be returned by using `Flow<T>`:
 *
 * ```kotlin
 * import io.github.kotlinmania.asyncstream.stream
 * import kotlinx.coroutines.flow.Flow
 * import kotlinx.coroutines.flow.collect
 * import kotlinx.coroutines.runBlocking
 *
 * fun zeroToThree(): Flow<Int> = stream {
 *     for (i in 0 until 3) {
 *         send(i)
 *     }
 * }
 *
 * fun main() = runBlocking {
 *     val s = zeroToThree()
 *     s.collect { value ->
 *         println("got $value")
 *     }
 * }
 * ```
 *
 * Streams may be implemented in terms of other streams. Where upstream uses
 * Rust's `for await` syntax, the Kotlin translation iterates with
 * `Flow.collect`:
 *
 * ```kotlin
 * import io.github.kotlinmania.asyncstream.stream
 * import kotlinx.coroutines.flow.Flow
 * import kotlinx.coroutines.flow.collect
 * import kotlinx.coroutines.runBlocking
 *
 * fun zeroToThree(): Flow<Int> = stream {
 *     for (i in 0 until 3) send(i)
 * }
 *
 * fun double(input: Flow<Int>): Flow<Int> = stream {
 *     input.collect { value -> send(value * 2) }
 * }
 *
 * fun main() = runBlocking {
 *     double(zeroToThree()).collect { value -> println("got $value") }
 * }
 * ```
 *
 * Rust try notation (`?`) maps to thrown exceptions inside [tryStream]. The
 * element type of the returned stream is `Result<T>` with `Result.success`
 * carrying the value yielded and `Result.failure` carrying any propagated
 * error.
 *
 * # Implementation
 *
 * The [stream] and [tryStream] builders are plain higher-order functions. The
 * block receives a [Sender] as the lambda receiver and calls `send($expr)` to
 * yield each value. The upstream proc-macro pass that rewrites `yield $expr`
 * into `sender.send($expr).await` is unnecessary because Kotlin already
 * carries the [Sender] in scope via the receiver type.
 *
 * The stream uses a lightweight rendezvous channel to send values from the
 * producer to the collector. `send(value)` parks the producer coroutine until
 * the collector resumes it, mirroring the two-step park/resume hand-off the
 * upstream `Send<T>` future hand-codes around its thread-local cell.
 *
 * [Flow]: kotlinx.coroutines.flow.Flow
 */

/**
 * Asynchronous stream.
 *
 * See the module documentation for more details.
 *
 * # Examples
 *
 * ```kotlin
 * import io.github.kotlinmania.asyncstream.stream
 * import kotlinx.coroutines.flow.collect
 * import kotlinx.coroutines.runBlocking
 *
 * fun main() = runBlocking {
 *     val s = stream<Int> {
 *         for (i in 0 until 3) send(i)
 *     }
 *
 *     s.collect { value -> println("got $value") }
 * }
 * ```
 */
fun <T> stream(block: suspend Sender<T>.() -> Unit): AsyncStream<T> {
    val (tx, rx) = pair<T>()
    return AsyncStream(rx) { tx.block() }
}
