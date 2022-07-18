@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package bk.github.insets

import android.graphics.Rect
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.marginBottom
import bk.github.tools.windowManager
import bk.github.tools.windowSize

@Suppress("NOTHING_TO_INLINE")
inline fun InsetsDelegate.Builder.applyImeMargin(
    consume: Boolean = false,
    animate: Boolean = true,
    noinline action: ((Int) -> Int)? = null
): InsetsDelegate.Builder = insets(ime = true)
    .consume(bottom = consume)
    .animate(bottom = animate)
    .let {
        it.build(object : InsetApplicator.MarginApplicator(it, InsetsSide.BOTTOM) {

            override fun getValue(
                view: View,
                windowInsets: WindowInsetsCompat,
                initialValues: Dimensions
            ): Dimensions {
                val r = Rect()
                if (!view.getGlobalVisibleRect(r)) return Dimensions.EMPTY

                r.offset(-view.translationX.toInt(), -view.translationY.toInt())
                val windowBottom =
                    view.context.windowManager!!.windowSize.y - getDimensions(windowInsets).bottom

                val bottom = view.marginBottom + (r.bottom - windowBottom)
                return Dimensions(
                    bottom = ((action?.invoke(bottom)
                        ?: bottom) - initialValues.bottom).coerceAtLeast(0)
                )
            }

        })
    }

@Suppress("NOTHING_TO_INLINE")
inline fun InsetsDelegate.Builder.applyImePadding(
    consume: Boolean = false,
    animate: Boolean = true,
    noinline action: ((Int) -> Int)? = null
): InsetsDelegate.Builder = insets(ime = true)
    .consume(bottom = consume)
    .animate(bottom = animate)
    .let {
        it.build(object : InsetApplicator.PaddingApplicator(it, InsetsSide.BOTTOM) {

            override fun getValue(
                view: View,
                windowInsets: WindowInsetsCompat,
                initialValues: Dimensions
            ): Dimensions {
                val child = (view as? ViewGroup)?.focusedChild ?: view
                val r = Rect()
                if (!child.getGlobalVisibleRect(r)) return Dimensions.EMPTY

                r.offset(-child.translationX.toInt(), -child.translationY.toInt())
                val windowBottom =
                    view.context.windowManager!!.windowSize.y - getDimensions(windowInsets).bottom

                val bottom = view.paddingBottom + (r.bottom - windowBottom)
                return Dimensions(
                    bottom = ((action?.invoke(bottom)
                        ?: bottom) - initialValues.bottom).coerceAtLeast(0)
                )
            }

        })
    }

