package bk.github.auth.animation

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.google.android.material.animation.AnimatorSetCompat

internal object CustomAnimators {

    fun alphaAnimator(duration: Long, endValue: Float): Animator =
        ObjectAnimator().apply {
            setProperty(View.ALPHA)
            setFloatValues(endValue)
            setDuration(duration)
            interpolator = LinearInterpolator()
        }

    fun fallAnimator(duration: Long, startValue: Float): Animator =
        ObjectAnimator().apply {
            setProperty(View.TRANSLATION_Y)
            setFloatValues(startValue, 0f)
            setDuration(duration)
            interpolator = LinearOutSlowInInterpolator()
        }

    fun waveAnimator(duration: Long, amplitude: Float, waves: Int): Animator {
        val fluctuations = FloatArray(waves * 2 + 1) {
            when (it % 4) {
                3 -> amplitude
                1 -> -amplitude
                else -> 0f
            }
        }

        return ObjectAnimator().apply {
            setProperty(View.TRANSLATION_X)
            setFloatValues(*fluctuations)
            setDuration(duration)
            interpolator = LinearInterpolator()
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    inline operator fun Animator.plus(other: Animator): Animator {
        val items = arrayListOf<Animator>()
        if (this is AnimatorSet) {
            items.addAll(this.childAnimations)
        } else {
            items.add(this)
        }
        items.add(other)
        return AnimatorSet().apply {
            AnimatorSetCompat.playTogether(this, items)
        }
    }

}