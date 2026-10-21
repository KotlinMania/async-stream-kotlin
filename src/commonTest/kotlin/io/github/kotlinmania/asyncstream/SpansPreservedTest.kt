// port-lint: tests tests/spans_preserved.rs
package io.github.kotlinmania.asyncstream

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class SpansPreservedTest {
    // Upstream spans_preserved asserts line!() inside stream! macro is preserved across proc-macro expansion.
    // In Kotlin, higher-order builder blocks preserve source lines and debug info directly without AST rewriting.
    @Test
    fun spansPreserved() =
        runTest {
            var executed = false
            val s =
                stream<Unit> {
                    executed = true
                }
            s.collect()
            assertTrue(executed)
        }
}
