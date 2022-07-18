@file:Suppress("unused", "NOTHING_TO_INLINE")

package bk.github.insets

import android.view.View

inline fun View.applyInsets(build: InsetsDelegate.Builder.() -> Unit): InsetsDelegate {
    return InsetsDelegate.Builder().apply(build).applyToView(this)
}