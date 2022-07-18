@file:Suppress("unused", "NOTHING_TO_INLINE")

package bk.github.tools

inline val Throwable.requireMessage get() = message ?: toString()

inline fun Throwable?.asException(): Exception? =
    if (this == null || this is Exception) {
        this as? Exception
    } else {
        Exception(message, cause)
    }

