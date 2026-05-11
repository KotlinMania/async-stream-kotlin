// port-lint: source src/async_stream.rs
package io.github.kotlinmania.asyncstream

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch

/**
 * Anonymous stream type returned by the [stream] and [tryStream] builders.
 *
 * Wraps the generator coroutine (`U` in upstream) together with the
 * [Receiver] side of the rendezvous channel. The generator is launched when
 * the stream is collected; each `tx.send(value)` inside the generator
 * delivers one item to the [FlowCollector] and suspends until the collector
 * resumes the producer.
 *
 * Implements [Flow] in place of upstream's `Stream` and `FusedStream` traits.
 * The `is_terminated` query from `FusedStream` is exposed as [isTerminated];
 * once the stream has finished producing, repeated collection emits nothing
 * and [isTerminated] returns `true`, mirroring the upstream "fused" guarantee
 * that further polls keep returning `Poll::Ready(None)`.
 *
 * `pin_project!` from upstream is unnecessary: Kotlin coroutines never expose
 * raw `Pin` projection, and the generator is driven by the collector's
 * coroutine scope.
 */
class AsyncStream<T> internal constructor(
    private val rx: Receiver<T>,
    private val generator: suspend () -> Unit,
) : Flow<T> {
    private var done: Boolean = false

    /** Returns `true` once the generator has run to completion. */
    fun isTerminated(): Boolean = done

    /**
     * Drive the wrapped generator to completion, emitting each yielded value
     * to `collector`.
     *
     * Upstream `poll_next` is a single-shot drive that swaps the receiver's
     * thread-local cell, polls the generator once, and inspects the cell. The
     * Kotlin translation launches the generator in a child coroutine and
     * iterates the rendezvous channel until the generator closes it on
     * completion: each iteration is the equivalent of one `poll_next` returning
     * `Poll::Ready(Some(value))`, and channel closure is the equivalent of
     * `Poll::Ready(None)` plus the upstream `*me.done = true` transition.
     */
    override suspend fun collect(collector: FlowCollector<T>) {
        if (done) return
        coroutineScope {
            val job = launch {
                try {
                    generator()
                } finally {
                    rx.channel.close()
                }
            }
            for (item in rx.channel) {
                collector.emit(item)
            }
            job.join()
            done = true
        }
    }

    /**
     * Upstream `size_hint` reports `(0, Some(0))` once terminated and
     * `(0, None)` otherwise. Kotlin [Flow] has no equivalent reporting
     * channel; callers that need the hint can branch on [isTerminated].
     */
    fun sizeHint(): Pair<Int, Int?> = if (done) 0 to 0 else 0 to null
}
