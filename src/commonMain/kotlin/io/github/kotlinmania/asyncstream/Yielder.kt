// port-lint: source yielder.rs
package io.github.kotlinmania.asyncstream

import io.github.kotlinmania.futures.Poll
import io.github.kotlinmania.futures.TaskContext
import kotlinx.coroutines.channels.Channel

/** Output type of the send future. */
public typealias Output = Unit

/**
 * Future that sends a value to the receiver.
 */
internal class Send<T>(
    private var value: T?,
    private val sender: Sender<T>,
) {
    fun poll(context: TaskContext): Poll<Unit> {
        val v = value ?: return Poll.Ready(Unit)
        return if (sender.channel.trySend(v).isSuccess) {
            value = null
            Poll.Ready(Unit)
        } else {
            context.wakeByRef()
            Poll.Pending
        }
    }
}

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
     * Send a value returning a Send future.
     */
    internal fun sendFuture(value: T): Send<T> = Send(value, this)
}

/**
 * Send `value` downstream and suspend until the collector resumes the producer.
 */
suspend fun <T> Sender<T>.send(value: T) {
    channel.send(value)
}

/**
 * Scope guard representing entering the receiver context.
 */
internal class Enter<T>(
    private val rx: Receiver<T>,
) : AutoCloseable {
    fun drop() {
        close()
    }

    override fun close() {
        // Restore context state
    }
}

/**
 * Receiver side of the stream value channel.
 *
 * Held privately by [AsyncStream] and consumed when the stream is collected.
 */
class Receiver<T> internal constructor(
    internal val channel: Channel<T>,
) {
    internal fun enter(): Enter<T> = Enter(this)
}

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
