// port-lint: source yielder.rs
package io.github.kotlinmania.asyncstream

import kotlinx.coroutines.channels.Channel

/**
 * Sender side of the stream value channel.
 *
 * Backed by a rendezvous channel: each [send] call suspends the producer
 * coroutine until the collector pulls the value out of the channel.
 */
class Sender<T> internal constructor(
    internal val channel: Channel<T>,
) {
    /**
     * Send `value` downstream and suspend until the collector resumes the producer.
     */
    suspend fun send(value: T) {
        channel.send(value)
    }
}

/**
 * Receiver side of the stream value channel.
 *
 * Held privately by [AsyncStream] and consumed when the stream is collected.
 */
class Receiver<T> internal constructor(
    internal val channel: Channel<T>,
)

/**
 * Create a paired [Sender]/[Receiver] backed by a rendezvous channel.
 *
 * Internal factory intended only for the [stream] and [tryStream] builders.
 */
internal fun <T> pair(): Pair<Sender<T>, Receiver<T>> {
    val channel = Channel<T>(Channel.RENDEZVOUS)
    val tx = Sender(channel)
    val rx = Receiver(channel)
    return tx to rx
}
