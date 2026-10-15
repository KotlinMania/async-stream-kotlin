// port-lint: source tests/for_await.rs
package io.github.kotlinmania.asyncstream

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ForAwaitTest {
    @Test
    fun test() =
        runTest {
            val s =
                stream<String> {
                    send("hello")
                    send("world")
                }

            val s2 =
                stream<String> {
                    s.collect { x -> send(x + "!") }
                }

            val values: List<String> = s2.toList()

            assertEquals(2, values.size)
            assertEquals("hello!", values[0])
            assertEquals("world!", values[1])
        }
}
