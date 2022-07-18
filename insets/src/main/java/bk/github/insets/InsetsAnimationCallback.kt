@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package bk.github.insets

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import bk.github.tools.forEachDescending
import bk.github.tools.globalRect

internal class InsetsAnimationCallback(
    private val view: View,
    private val animateInfo: InsetsAnimateInfo,
    private val onInsetsAnimationListener: InsetsDelegate.OnInsetsAnimationListener? = null
) : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_STOP) {

    private val viewAnimates: MutableList<ViewAnimator> = mutableListOf()

    override fun onPrepare(animation: WindowInsetsAnimationCompat) {
        super.onPrepare(animation)
        onInsetsAnimationListener?.onInsetsAnimation()
        if (animation.typeMask and animateInfo.marginsTypes != 0) {
            viewAnimates.add(ViewAnimator(view, animateInfo.marginsSides))
        }
        if (animation.typeMask and animateInfo.paddingsTypes != 0) {
            (view as? ViewGroup)?.children?.forEach {
                viewAnimates.add(ViewAnimator(it, animateInfo.paddingsSides))
            }
        }
    }

    override fun onStart(
        animation: WindowInsetsAnimationCompat,
        bounds: WindowInsetsAnimationCompat.BoundsCompat
    ): WindowInsetsAnimationCompat.BoundsCompat {
        return super.onStart(animation, bounds).also {
            onInsetsAnimationListener?.onInsetsAnimation()
            viewAnimates.forEachDescending {
                if (!it.startAnimation()) viewAnimates.remove(it)
            }
        }
    }

    override fun onProgress(
        insets: WindowInsetsCompat,
        runningAnimations: MutableList<WindowInsetsAnimationCompat>
    ): WindowInsetsCompat {
        onInsetsAnimationListener?.onInsetsAnimation()
        if (runningAnimations.isNotEmpty()) {
            val fraction = 1 - runningAnimations.maxOf { it.fraction }
            viewAnimates.forEach { it.animate(fraction) }
        }

        return insets
    }

    override fun onEnd(animation: WindowInsetsAnimationCompat) {
        super.onEnd(animation)
        onInsetsAnimationListener?.onInsetsAnimation()
        viewAnimates.forEach { it.endAnimation() }
        viewAnimates.clear()
    }

    private class ViewAnimator(
        val view: View,
        @InsetsSide.Sides val sides: Int
    ) {
        private val startBounds: Dimensions =
            with(view.globalRect) { Dimensions(left, top, right, bottom) }
        private var moveDx: Int = 0
        private var moveDy: Int = 0

        fun startAnimation(): Boolean {
            val rect = view.globalRect
            val moveLeft =
                if (sides and InsetsSide.LEFT != 0) 0 else startBounds.left - rect.left
            val moveTop =
                if (sides and InsetsSide.TOP != 0) 0 else startBounds.top - rect.top
            val moveRight =
                if (sides and InsetsSide.RIGHT != 0) 0 else startBounds.right - rect.right
            val moveBottom =
                if (sides and InsetsSide.BOTTOM != 0) 0 else startBounds.bottom - rect.bottom

            moveDx = (moveLeft + moveRight) / 2
            moveDy = (moveTop + moveBottom) / 2
            return moveDx != 0 || moveDy != 0
        }

        fun animate(fraction: Float) {
            if (view.isVisible) {
                view.translationX = moveDx * fraction
                view.translationY = moveDy * fraction
            }
        }

        fun endAnimation() {
            view.translationX = 0f
            view.translationY = 0f
        }
    }
}