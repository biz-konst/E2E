@file:Suppress("unused", "NOTHING_TO_INLINE")

package bk.github.tools

import android.graphics.Rect
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop

inline val View.inflater: LayoutInflater get() = LayoutInflater.from(context)

inline fun View.doOnFocusChanged(crossinline action: (view: View, hasFocus: Boolean) -> Unit) =
    apply {
        onFocusChangeListener =
            View.OnFocusChangeListener { view, hasFocus -> action(view, hasFocus) }
    }

inline fun View.doOnLostFocus(crossinline action: View.() -> Unit) =
    doOnFocusChanged { view, hasFocus -> if (!hasFocus) action(view) }

inline fun View.doOnClick(noinline action: View.() -> Unit) =
    setOnClickListener(object : View.OnClickListener {
        private var clicked = false

        override fun onClick(view: View) {
            if (!clicked) {
                clicked = true
                try {
                    action(view)
                } finally {
                    clicked = false
                }
            }
        }
    })

fun View.updateMargins(
    left: Int = marginLeft,
    top: Int = marginTop,
    right: Int = marginRight,
    bottom: Int = marginBottom
): Boolean {
    (layoutParams as? ViewGroup.MarginLayoutParams)?.let {
        if (it.leftMargin != left || it.topMargin != top ||
            it.rightMargin != right || it.bottomMargin != bottom
        ) {
            it.setMargins(left, top, right, bottom)
            layoutParams = it

            if (Build.VERSION.SDK_INT < 26) {
                // See https://github.com/chrisbanes/insetter/issues/42
                parent?.requestLayout()
            }
            return true
        }
    }
    return false
}

val View.globalRect: Rect
    get() {
        val r = Rect()
        getGlobalVisibleRect(r)
        r.offset(-translationX.toInt(), -translationY.toInt())
        return r
    }