// port-lint: source tests/try_stream.rs
package io.github.kotlinmania.asyncstream

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TryStreamTest {
    @Test
    fun singleErr() = runTest {
        val cond: Boolean = run { true }
        val s = tryStream<String> {
            if (cond) {
                throw HelloException()
            } else {
                send(Result.success("world"))
            }
        }

        val values = s.toList()
        assertEquals(1, values.size)
        assertTrue(values[0].exceptionOrNull() is HelloException)
    }

    @Test
    fun yieldThenErr() = runTest {
        val s = tryStream<String> {
            send(Result.success("hello"))
            throw WorldException()
        }

        val values = s.toList()
        assertEquals(2, values.size)
        assertEquals("hello", values[0].getOrThrow())
        assertTrue(values[1].exceptionOrNull() is WorldException)
    }

    @Test
    fun convertErr() = runTest {
        fun test(): Flow<Result<String>> = tryStream {
            // Upstream converts ErrorA into ErrorB via `From<ErrorA> for ErrorB`
            // and Rust's `?` operator. The Kotlin translation throws the
            // already-converted exception, which tryStream forwards as
            // `Result.failure`.
            throw ErrorB(1)
        }

        val values = test().toList()
        assertEquals(1, values.size)
        assertEquals(ErrorB(1), values[0].exceptionOrNull())
    }

    @Test
    fun multiTry() = runTest {
        fun test(): Flow<Result<Int>> = tryStream {
            val outer: Result<Result<Int>> = Result.success(Result.success(123))
            val a = outer.getOrThrow().getOrThrow()
            for (unused in 1 until 10) {
                send(Result.success(a))
            }
        }

        val values = test().toList()
        assertEquals(9, values.size)
        for (v in values) {
            assertEquals(123, v.getOrThrow())
        }
    }
}

private class HelloException : RuntimeException("hello")
private class WorldException : RuntimeException("world")

private data class ErrorB(val code: Int) : RuntimeException()
