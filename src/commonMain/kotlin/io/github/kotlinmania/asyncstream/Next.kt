// port-lint: source src/next.rs
package io.github.kotlinmania.asyncstream

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

/**
 * Pull the next item from `stream`, or `null` if the stream has terminated.
 *
 * This is equivalent to `kotlinx.coroutines.flow.firstOrNull` on a [Flow], but
 * we keep our local helper so this crate's dependency surface stays as small
 * as possible.
 *
 * Upstream's helper is generic over any unpinnable stream type and returns a
 * future that mutably borrows the stream and polls it exactly once. The Kotlin
 * translation accepts any [Flow] and reads its first emission. Type parameter
 * `T` is constrained to [Any] because [firstOrNull] uses `null` as the
 * end-of-stream sentinel and would otherwise collide with a legitimate `null`
 * value.
 */
internal suspend fun <T : Any> next(stream: Flow<T>): T? = stream.firstOrNull()
