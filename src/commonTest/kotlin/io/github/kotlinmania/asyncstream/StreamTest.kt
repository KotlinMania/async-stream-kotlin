// port-lint: source tests/stream.rs
package io.github.kotlinmania.asyncstream

import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

class StreamTest {
    @Test
    fun noopStream() = runTest {
        val s = stream<Int> {}
        s.collect { fail("unreachable") }
    }

    @Test
    fun emptyStream() = runTest {
        var ran = false

        val s = stream<Int> {
            ran = true
            // Equivalent to upstream's println!("hello world!"); no observable
            // effect beyond setting the flag.
        }
        s.collect { fail("unreachable") }

        assertTrue(ran)
    }

    @Test
    fun yieldSingleValue() = runTest {
        val s = stream<String> {
            send("hello")
        }

        val values = s.toList()

        assertEquals(1, values.size)
        assertEquals("hello", values[0])
    }

    @Test
    fun fused() = runTest {
        // streamInternal returns the concrete AsyncStream so this test can
        // observe its fused-after-exhaustion flag; the public `stream` builder
        // erases to Flow<T>, but the fused behavior the public flow exposes is
        // proven below by the empty-list-on-second-collect assertion.
        val s = streamInternal<String> {
            send("hello")
        }

        assertFalse(s.isTerminated())
        assertEquals(listOf("hello"), s.toList())
        assertTrue(s.isTerminated())

        // Once terminated, further collection emits nothing. Upstream's
        // fused-stream contract says repeated pulls after termination keep
        // returning the terminal "no more values" signal; here the equivalent
        // is that re-collecting an exhausted stream is a no-op.
        assertEquals(emptyList(), s.toList())
        assertTrue(s.isTerminated())
    }

    @Test
    fun yieldMultiValue() = runTest {
        val s = stream<String> {
            send("hello")
            send("world")
            send("dizzy")
        }

        val values = s.toList()

        assertEquals(3, values.size)
        assertEquals("hello", values[0])
        assertEquals("world", values[1])
        assertEquals("dizzy", values[2])
    }

    @Test
    fun returnStream() = runTest {
        fun buildStream(): Flow<Int> = stream {
            send(1)
            send(2)
            send(3)
        }

        val values = buildStream().toList()
        assertEquals(3, values.size)
        assertEquals(1, values[0])
        assertEquals(2, values[1])
        assertEquals(3, values[2])
    }

    @Test
    fun streamInStream() = runTest {
        val s = stream<Int> {
            val inner = stream<Int> {
                for (i in 0 until 3) send(i)
            }
            inner.collect { send(it) }
        }

        val values = s.toList()
        assertEquals(3, values.size)
    }

    @Test
    fun unitYieldInSelect() = runTest {
        suspend fun doStuffAsync() {}

        val s = stream<Unit> {
            coroutineScope {
                val d = async { doStuffAsync() }
                select<Unit> {
                    d.onAwait { send(Unit) }
                }
            }
        }

        val values = s.toList()
        assertEquals(1, values.size)
    }

    @Test
    fun yieldWithSelect() = runTest {
        suspend fun doStuffAsync() {}
        suspend fun moreAsyncWork() {}

        val s = stream<String> {
            coroutineScope {
                val d1 = async { doStuffAsync() }
                val d2 = async { moreAsyncWork() }
                select<Unit> {
                    d1.onAwait { send("hey") }
                    d2.onAwait { send("hey") }
                }
            }
        }

        val values = s.toList()
        assertEquals(listOf("hey"), values)
    }

    @Test
    fun consumeChannel() = runTest {
        val ch = Channel<Int>(capacity = 10)

        val s = stream<Int> {
            for (v in ch) send(v)
        }

        val producer = launch {
            for (i in 0 until 3) ch.send(i)
            ch.close()
        }

        val values = s.toList()
        producer.join()
        assertEquals(listOf(0, 1, 2), values)
    }

    @Test
    fun borrowSelf() = runTest {
        // Upstream's `borrow_self` exercises Rust's `&'a self` borrow in a
        // stream that yields a slice of the receiver. The Kotlin analog is a
        // captured property: the lambda holds a reference to the instance for
        // the stream's lifetime; the observable property is that yielding a
        // self-derived value works.
        class Data(val value: String) {
            fun asStream(): Flow<String> = stream {
                send(this@Data.value)
            }
        }

        val data = Data("hello")
        assertEquals(listOf("hello"), data.asStream().toList())
    }

    @Test
    fun innerTryStream() = runTest {
        // Upstream's `inner_try_stream` is a compile-only check that
        // `try_stream!` composes inside `stream!` inside a `select!` arm.
        // This port exercises the same composition and additionally consumes
        // the inner try-stream's first element, since Kotlin type-checks at
        // compile time and a runtime exercise is a strictly stronger signal.
        suspend fun doStuffAsync() {}

        val s = stream<Unit> {
            coroutineScope {
                val d = async { doStuffAsync() }
                select<Unit> {
                    d.onAwait {
                        val inner = tryStream<Unit> { send(Result.success(Unit)) }
                        inner.first()
                        send(Unit)
                    }
                }
            }
        }

        val values = s.toList()
        assertEquals(1, values.size)
    }

    // Upstream `yield_non_unpin_value` yields `async move { i }` futures
    // through `.buffered(1)`. The test depends on Rust's Pin/!Unpin
    // move-during-poll semantics and on `.buffered(N)`'s pull-concurrent-
    // futures behavior. kotlinx.coroutines.flow has no direct analog: a
    // flow of suspend lambdas resolved via `flatMapMerge(concurrency = 1)`
    // would test a different invariant (sequential collapse of pre-launched
    // coroutines, not Pin-aware future polling). Left unported per AGENTS.md
    // §6 and feedback_dont_infer_ports.
}
