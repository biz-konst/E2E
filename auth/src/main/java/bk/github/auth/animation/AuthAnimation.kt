package bk.github.auth.animation

import android.animation.Animator
import android.view.View
import android.widget.TextView
import androidx.core.animation.doOnEnd
import bk.github.auth.animation.CustomAnimators.alphaAnimator
import bk.github.auth.animation.CustomAnimators.fallAnimator
import bk.github.auth.animation.CustomAnimators.plus
import bk.github.auth.animation.CustomAnimators.waveAnimator
import bk.github.auth.pincode.ui.PinCodeFragment
import bk.github.tools.TextAnimator

class AuthAnimation : PinCodeFragment.Animation {

    companion object {
        private const val FADE_IN_ANIMATION_DURATION = 167L
        private const val FADE_OUT_ANIMATION_DURATION = 167L
        private const val FALL_ANIMATION_DURATION = 217L
        private const val SHAKE_ANIMATION_DURATION = 117L
        private const val SHAKE_ANIMATION_AMPLITUDE = 10f
    }

    private var pinCodeAnimator: Animator? = null
    private var helperTextAnimator: TextAnimator? = null
    private var errorTextAnimator: TextAnimator? = null
    private var progressTextAnimator: TextAnimator? = null
    private var delayTextAnimator: TextAnimator? = null

    override fun animatePinCode(target: View, failed: Boolean) {
        if (failed) {
            (pinCodeAnimator
                ?: waveAnimator(SHAKE_ANIMATION_DURATION, SHAKE_ANIMATION_AMPLITUDE, 2)
                    .also { pinCodeAnimator = it })
                .apply {
                    setTarget(target)
                    start()
                }
        } else {
            pinCodeAnimator?.cancel()
        }
    }

    override fun animateErrorText(target: TextView, text: String?, visible: Boolean) {
        (errorTextAnimator ?: TextAnimator(
            target,
            animateTextChange = false,
            showAnimation = { fadeIn(target) + fall(target) },
            hideAnimation = ::fadeOut,
            hideChangedAnimation = {
                val k = target.alpha
                alphaAnimator((FADE_OUT_ANIMATION_DURATION * k).toLong(), 0f)
            },
            showChangedAnimation = { _, first ->
                val k = if (first) 1 - target.alpha else 1f
                alphaAnimator((FADE_OUT_ANIMATION_DURATION * k).toLong(), 1f)
            },
        ).also { errorTextAnimator = it })
            .start(text, visible)
    }

    override fun animateHelperText(target: TextView, text: String?, visible: Boolean) {
        (helperTextAnimator ?: TextAnimator(
            target,
            animateTextChange = false,
            showAnimation = ::fadeIn,
            hideAnimation = ::fadeOut,
        ).also { helperTextAnimator = it })
            .start(text, visible)
    }

    override fun animateProgressText(target: TextView, text: String?, visible: Boolean) {
        (progressTextAnimator ?: TextAnimator(
            target,
            animateTextChange = false,
            showAnimation = ::fadeIn,
            hideAnimation = ::fadeOut,
        ).also { progressTextAnimator = it })
            .start(text, visible)
    }

    override fun animateDelayText(target: TextView, text: String?, visible: Boolean) {
        (delayTextAnimator ?: TextAnimator(
            target,
            animateTextChange = false,
            showAnimation = { fadeIn(target) + fall(target) },
            hideAnimation = ::fadeOut,
        ).also { delayTextAnimator = it })
            .start(text, visible)
    }

    override fun cancelPinCodeAnimation() {
        pinCodeAnimator?.let {
            it.cancel()
            pinCodeAnimator = null
        }
    }

    override fun cancelErrorTextAnimation() {
        errorTextAnimator?.let {
            it.cancel()
            errorTextAnimator = null
        }
    }

    override fun cancelHelperTextAnimation() {
        helperTextAnimator?.let {
            it.cancel()
            helperTextAnimator = null
        }
    }

    override fun cancelProgressTextAnimation() {
        progressTextAnimator?.let {
            it.cancel()
            progressTextAnimator = null
        }
    }

    override fun cancelDelayTextAnimation() {
        delayTextAnimator?.let {
            it.cancel()
            delayTextAnimator = null
        }
    }

    private fun fall(target: View): Animator {
        return fallAnimator(FALL_ANIMATION_DURATION, -target.measuredHeight.toFloat())
            .apply { doOnEnd { target.translationY = 0f } }
    }

    private fun fadeIn(target: View): Animator {
        target.alpha = 0f
        return alphaAnimator(FADE_IN_ANIMATION_DURATION, 1f)
    }

    private fun fadeOut(target: View): Animator {
        return alphaAnimator(FADE_OUT_ANIMATION_DURATION, 0f)
            .apply { doOnEnd { target.alpha = 1f } }
    }

}