@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package bk.github.insets

import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.Insets
import androidx.core.view.*

class InsetsDelegate private constructor(
    applicators: List<InsetApplicator> = emptyList(),
    private val onApplyInsetsListener: OnApplyInsetsListener? = null,
    private val onInsetsAnimationListener: OnInsetsAnimationListener? = null
) : OnApplyWindowInsetsListener, View.OnAttachStateChangeListener {

    fun interface OnApplyInsetsListener {
        fun onApplyInsets(
            view: View,
            windowInsets: WindowInsetsCompat,
            initialState: ViewState
        ): WindowInsetsCompat
    }

    fun interface OnInsetsAnimationListener {
        fun onInsetsAnimation()
    }

    private val marginApplicators: List<InsetApplicator.MarginApplicator> =
        applicators.filterIsInstance<InsetApplicator.MarginApplicator>()
    private val paddingApplicators: List<InsetApplicator.PaddingApplicator> =
        applicators.filterIsInstance<InsetApplicator.PaddingApplicator>()

    private val animateInfo = InsetsAnimateInfo(
        marginsTypes = marginApplicators.fold(0) { acc, it -> acc or it.types },
        marginsSides = marginApplicators.fold(0) { acc, it -> acc or it.animate },
        paddingsTypes = paddingApplicators.fold(0) { acc, it -> acc or it.types },
        paddingsSides = paddingApplicators.fold(0) { acc, it -> acc or it.animate }
    )

    private var lastInsets: WindowInsetsCompat = WindowInsetsCompat.CONSUMED

    override fun onApplyWindowInsets(
        view: View,
        windowInsets: WindowInsetsCompat
    ): WindowInsetsCompat {
        val viewState = view.viewState

        val insets =
            onApplyInsetsListener?.onApplyInsets(view, windowInsets, viewState) ?: windowInsets
        if (insets == WindowInsetsCompat.CONSUMED) return insets

        if (lastInsets != insets) {
            // fix a bug for api < 30, when ApplyWindowInsets is called for the first time
            // after attach, the view position does not change after setting margins
            applyMargins(view, insets, viewState.margins)
            applyPaddings(view, insets, viewState.paddings)
        }

        return consume(insets).also { lastInsets = insets }
    }

    override fun onViewAttachedToWindow(view: View) {
        view.doOnLayout { ViewCompat.requestApplyInsets(view) }
    }

    override fun onViewDetachedFromWindow(view: View) = Unit

    fun applyToView(view: View): InsetsDelegate {
        ViewCompat.setOnApplyWindowInsetsListener(view, this)
        if (!animateInfo.isEmpty()) {
            ViewCompat.setWindowInsetsAnimationCallback(
                view,
                InsetsAnimationCallback(view, animateInfo, onInsetsAnimationListener)
            )
        }

        view.removeOnAttachStateChangeListener(this)
        view.addOnAttachStateChangeListener(this)
        if (view.isLaidOut) {
            onViewAttachedToWindow(view)
        }

        return this
    }

    private fun consume(insets: WindowInsetsCompat): WindowInsetsCompat =
        WindowInsetsCompat.Builder(insets).apply {
            marginApplicators.forEach { consumeTypes(it.types, insets, it.consume) }
            paddingApplicators.forEach { consumeTypes(it.types, insets, it.consume) }
        }.build()

    private fun applyMargins(
        view: View,
        windowInsets: WindowInsetsCompat,
        initialMargins: Dimensions
    ) {
        val lp = view.layoutParams as? ViewGroup.MarginLayoutParams ?: return

        val margins = initialMargins + marginApplicators.fold(Dimensions.EMPTY) { acc, it ->
            acc.max(it.getValue(view, windowInsets, initialMargins))
        }

        if (lp.leftMargin != margins.left || lp.topMargin != margins.top ||
            lp.rightMargin != margins.right || lp.bottomMargin != margins.bottom
        ) {
            lp.setMargins(margins.left, margins.top, margins.right, margins.bottom)
            view.layoutParams = lp

            if (Build.VERSION.SDK_INT < 26) {
                // See https://github.com/chrisbanes/insetter/issues/42
                view.parent?.requestLayout()
            }
        }
    }

    private fun applyPaddings(
        view: View,
        windowInsets: WindowInsetsCompat,
        initialPaddings: Dimensions
    ) {
        val paddings = initialPaddings + paddingApplicators.fold(Dimensions.EMPTY) { acc, it ->
            acc.max(it.getValue(view, windowInsets, initialPaddings))
        }

        view.setPadding(paddings.left, paddings.top, paddings.right, paddings.bottom)
    }

    class Builder {

        private val applicators: MutableSet<InsetApplicator> = mutableSetOf()
        private var onApplyInsetsListener: OnApplyInsetsListener? = null
        private var onInsetsAnimationListener: OnInsetsAnimationListener? = null

        fun addApplicator(applicator: InsetApplicator): Builder = apply {
            require(applicator.types != 0 && applicator.apply != InsetsSide.NONE) {
                "Insets applicator cannot be an empty types and must contain at least one side"
            }
            applicators.add(applicator)
        }

        fun setApplyInsetsListener(listener: OnApplyInsetsListener? = null): Builder =
            apply { onApplyInsetsListener = listener }

        fun setInsetAnimationListener(listener: OnInsetsAnimationListener? = null): Builder =
            apply { onInsetsAnimationListener = listener }

        fun build(): InsetsDelegate {
            require(applicators.isNotEmpty() || onApplyInsetsListener != null) {
                "At least one applicator or listener must be specified"
            }
            return InsetsDelegate(
                applicators.toList(), onApplyInsetsListener, onInsetsAnimationListener
            )
        }

        fun applyToView(view: View): InsetsDelegate = build().applyToView(view)

    }

    private fun InsetsAnimateInfo.isEmpty() =
        (marginsTypes == 0 || marginsSides == InsetsSide.NONE) &&
                (paddingsTypes == 0 || paddingsSides == InsetsSide.NONE)

    private fun WindowInsetsCompat.Builder.consumeTypes(
        types: Int,
        windowInsets: WindowInsetsCompat,
        @InsetsSide.Sides sides: Int
    ): WindowInsetsCompat.Builder {
        if (sides == InsetsSide.NONE) return this

        val insets = windowInsets.getInsets(types)
        if (insets == Insets.NONE) return this

        return setInsets(
            types, Insets.of(
                if (sides and InsetsSide.LEFT != 0) 0 else insets.left,
                if (sides and InsetsSide.TOP != 0) 0 else insets.top,
                if (sides and InsetsSide.RIGHT != 0) 0 else insets.right,
                if (sides and InsetsSide.BOTTOM != 0) 0 else insets.bottom,
            )
        )
    }

}

