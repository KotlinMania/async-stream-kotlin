// port-lint: source src/yielder.rs
package io.github.kotlinmania.asyncstream

import kotlinx.coroutines.channels.Channel

/**
 * Sender side of the stream value channel.
 *
 * Upstream's `Sender` carries a phantom marker because the value flow goes
 * through a raw pointer stored in a thread local. The Kotlin translation
 * replaces both with a rendezvous channel: each [send] call suspends the
 * producer coroutine until the collector pulls the value out of the channel.
 */
class Sender<T> internal constructor(internal val channel: Channel<T>) {
    /**
     * Send `value` downstream and suspend until the collector resumes the
     * producer.
     *
     * Upstream returns a single-poll future. The translation collapses that to
     * a plain suspend call because the rendezvous channel already provides the
     * two-step "park then resume" semantics that upstream's send future hand
     * codes around the thread-local cell.
     */
    suspend fun send(value: T) {
        channel.send(value)
    }
}

/**
 * Receiver side of the stream value channel.
 *
 * Held privately by [AsyncStream] and consumed when the stream is collected.
 * Upstream's `Receiver` carries a phantom marker only; the runtime machinery
 * is the same channel handle the [Sender] writes to.
 *
 * The upstream lifetime-guard helper on the receiver and the surrounding RAII
 * scope type, together with the thread-local cell that holds the in-flight
 * value, are Rust-only plumbing for the raw-pointer hand-off and have no
 * Kotlin counterpart: the channel rendezvous itself is the hand-off.
 */
class Receiver<T> internal constructor(internal val channel: Channel<T>)

/**
 * Create a paired [Sender]/[Receiver] backed by a rendezvous channel.
 *
 * It is considered unsound for anyone other than the [stream] and [tryStream]
 * builders to call this function. This is a private API intended only for
 * those builders, and users should never call it, but some people tend to
 * misinterpret it as fine to call unless it is marked unsafe.
 */
internal fun <T> pair(): Pair<Sender<T>, Receiver<T>> {
    val channel = Channel<T>(Channel.RENDEZVOUS)
    val tx = Sender(channel)
    val rx = Receiver(channel)
    return tx to rx
}
