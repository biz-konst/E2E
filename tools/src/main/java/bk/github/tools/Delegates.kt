@file:Suppress("NOTHING_TO_INLINE")

package bk.github.tools

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

inline fun <T> synchronized(initialValue: T) = object : ReadWriteProperty<Any, T> {
    private var field: T = initialValue

    override fun getValue(thisRef: Any, property: KProperty<*>): T =
        synchronized(this) { field }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        synchronized(this) { field = value }
    }
}

