// port-lint: source tests/stream.rs
package io.github.kotlinmania.asyncstream

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
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
        val s = stream<String> {
            send("hello")
        }

        assertFalse(s.isTerminated())
        assertEquals(listOf("hello"), s.toList())
        assertTrue(s.isTerminated())

        // Once terminated, further collection emits nothing. The upstream
        // `FusedStream` contract says repeated `poll_next` calls after
        // termination keep returning `Poll::Ready(None)`; here the equivalent
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
}
