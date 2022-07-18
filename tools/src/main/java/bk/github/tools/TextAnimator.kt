package bk.github.tools

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.view.View
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.doOnDetach
import androidx.core.view.isVisible

class TextAnimator(
    var view: TextView,
    private val animateTextChange: Boolean,
    private val showAnimation: ((view: View) -> Animator)? = null,
    private val hideAnimation: ((view: View) -> Animator)? = null,
    private val hideChangedAnimation: ((view: View) -> Animator)? = null,
    private val showChangedAnimation: ((view: View, isFirst: Boolean) -> Animator)? = null
) {

    private var animatedText: String? = view.text.toString()
    private var animatedVisible = view.isVisible
    private var animator: Animator? = null

    init {
        view.doOnDetach { cancel() }
    }

    fun start(text: String?, visible: Boolean) {

        if (animatedVisible != visible) {
            animatedVisible = visible
            animatedText = text

            if (visible) {
                view.text = animatedText
                view.isVisible = visible
                view.bringToFront()

                if (!animatedText.isNullOrEmpty()) {
                    animateShow()
                }

            } else {

                if (!view.text.isNullOrEmpty()) {
                    animateHide()
                    return
                }

                view.isVisible = visible
                view.text = animatedText
            }

        } else if (animatedText != text) {
            animatedText = text

            if (view.isVisible) {

                // ждем завершения анимации скрытия, там все будет установлено
                if (!animatedVisible && animator?.isStarted == true) {
                    return
                }

                if (animateTextChange
                    || animatedText.isNullOrEmpty() || view.text.isNullOrEmpty()
                ) {
                    animateChange()
                    return
                }

            }

            view.text = animatedText
        }

    }

    fun cancel() {
        animator?.let {
            when (it) {
                is AnimatorSet -> it.end()
                is ValueAnimator -> it.end()
                else -> it.cancel()
            }
            animator = null
        }
    }

    private fun animateShow() {
        animator?.cancel()
        animator = showAnimation?.invoke(view)?.apply {
            setTarget(view)
            start()
        }
    }

    private fun animateHide() {
        animator?.cancel()
        animator = hideAnimation?.invoke(view)?.apply {
            setTarget(view)
            doOnEnd {
                view.isVisible = false
                view.text = animatedText
            }
            start()
        }
    }

    private fun animateChange() {
        animator?.cancel()

        val animationList = arrayListOf<Animator>()

        if (!view.text.isNullOrEmpty() && view.text != animatedText) {
            hideChangedAnimation?.invoke(view)?.let {
                animationList.add(it)
            }
        }

        if (!animatedText.isNullOrEmpty()) {
            showChangedAnimation?.invoke(view, animationList.isEmpty())?.let {
                it.doOnStart {
                    view.text = animatedText
                }
                animationList.add(it)
            }
        }

        animator = AnimatorSet().apply {
            playSequentially(animationList)
            setTarget(view)
            doOnEnd { view.text = animatedText }
            start()
        }
    }

}