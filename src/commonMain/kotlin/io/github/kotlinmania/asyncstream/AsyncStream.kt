// port-lint: source src/async_stream.rs
package io.github.kotlinmania.asyncstream

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch

/**
 * Anonymous stream type returned by the [stream] and [tryStream] builders.
 *
 * Wraps the generator coroutine together with the [Receiver] side of the
 * rendezvous channel. The generator is launched when the stream is collected;
 * each `send(value)` call inside the generator delivers one item to the
 * [FlowCollector] and suspends until the collector resumes the producer.
 *
 * Upstream models the stream as two separate traits (one for the per-element
 * pull, one for the terminal-state flag). The Kotlin translation implements
 * [Flow] for the pull side and exposes the terminal-state flag through
 * [isTerminated]. Once the stream has finished producing, repeated collection
 * emits nothing and [isTerminated] returns `true`, mirroring upstream's
 * fused-after-exhaustion guarantee.
 *
 * The pin-projection macro upstream uses to safely move references into the
 * polling body is unnecessary: Kotlin coroutines never expose raw self-pinning,
 * and the generator runs inside the collector's coroutine scope.
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
     * Upstream's per-element pull is a single-shot drive that swaps the
     * receiver's thread-local cell, runs the generator one step, and inspects
     * the cell. The Kotlin translation launches the generator in a child
     * coroutine and iterates the rendezvous channel until the generator closes
     * it on completion: each iteration is the equivalent of one upstream pull
     * yielding a ready value, and channel closure is the equivalent of the
     * terminal "no more values" signal together with the transition that sets
     * the terminal-state flag.
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
     * Upstream's size-hint helper reports an exact `(0, 0)` window once the
     * stream is terminated and an open `(0, unbounded)` window otherwise.
     * Kotlin [Flow] has no equivalent reporting channel; callers that need the
     * hint can branch on [isTerminated].
     */
    fun sizeHint(): Pair<Int, Int?> = if (done) 0 to 0 else 0 to null
}
