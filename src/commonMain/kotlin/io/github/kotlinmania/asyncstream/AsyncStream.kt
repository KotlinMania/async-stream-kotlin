// port-lint: source async_stream.rs
package io.github.kotlinmania.asyncstream

import io.github.kotlinmania.futures.FusedStream
import io.github.kotlinmania.futures.Poll
import io.github.kotlinmania.futures.SizeHint
import io.github.kotlinmania.futures.TaskContext
import io.github.kotlinmania.futures.Yield
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch

/**
 * Item type of the stream.
 */
public typealias Item = Any?

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
internal class AsyncStream<T> internal constructor(
    private val rx: Receiver<T>,
    private val generator: suspend () -> Unit,
) : Flow<T>,
    FusedStream<T> {
    private var done: Boolean = false

    companion object {
        fun <T> new(rx: Receiver<T>, generator: suspend () -> Unit): AsyncStream<T> =
            AsyncStream(rx, generator)
    }

    /** Returns `true` once the generator has run to completion. */
    override fun isTerminated(): Boolean = done

    /**
     * Poll the next item from this stream.
     */
    override fun pollNext(context: TaskContext): Poll<Yield<T>> {
        if (done) return Poll.Ready(Yield.End)
        val result = rx.channel.tryReceive()
        return if (result.isSuccess) {
            Poll.Ready(Yield.Value(result.getOrThrow()))
        } else if (result.isClosed) {
            done = true
            Poll.Ready(Yield.End)
        } else {
            Poll.Pending
        }
    }

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
            val job =
                launch {
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
     */
    override fun sizeHint(): SizeHint = if (done) SizeHint(0, 0) else SizeHint(0, null)
}
