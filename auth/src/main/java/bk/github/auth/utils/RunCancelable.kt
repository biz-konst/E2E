@file:Suppress("unused")

package bk.github.auth.utils

inline fun <R> runSafely(block: () -> Result<R>): Result<R> {
    return try {
        block()
    } catch (e: Throwable) {
        Result.failure(e)
    }
}

inline fun <R, T> T.runSafely(block: T.() -> Result<R>): Result<R> {
    return try {
        block()
    } catch (e: Throwable) {
        Result.failure(e)
    }
}

