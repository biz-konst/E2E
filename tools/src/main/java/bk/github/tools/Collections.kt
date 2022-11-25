@file:Suppress("unused", "NOTHING_TO_INLINE")

package bk.github.tools

inline fun <T> List<T>.forEachDescending(action: (T) -> Unit) {
    for (i in size - 1 downTo 0) action(get(i))
}
