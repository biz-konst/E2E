@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package bk.github.insets

import android.view.View
import androidx.core.view.WindowInsetsCompat

sealed class InsetApplicator private constructor(
    val types: Int,
    val ignoreVisibility: Boolean = false,
    @InsetsSide.Sides val apply: Int = InsetsSide.NONE,
    @InsetsSide.Sides val consume: Int = InsetsSide.NONE,
    @InsetsSide.Sides val animate: Int = InsetsSide.NONE
) {

    fun getDimensions(windowInsets: WindowInsetsCompat): Dimensions {
        val insets = windowInsets.getInsets(types, ignoreVisibility)
        return Dimensions(
            left = if (apply and InsetsSide.LEFT == 0) 0 else insets.left,
            top = if (apply and InsetsSide.TOP == 0) 0 else insets.top,
            right = if (apply and InsetsSide.RIGHT == 0) 0 else insets.right,
            bottom = if (apply and InsetsSide.BOTTOM == 0) 0 else insets.bottom
        )
    }

    open fun getValue(
        view: View,
        windowInsets: WindowInsetsCompat,
        initialValues: Dimensions
    ): Dimensions = getDimensions(windowInsets)

    open class MarginApplicator(
        types: Int,
        ignoreVisibility: Boolean = false,
        @InsetsSide.Sides apply: Int = InsetsSide.NONE,
        @InsetsSide.Sides consume: Int = InsetsSide.NONE,
        @InsetsSide.Sides animate: Int = InsetsSide.NONE
    ) : InsetApplicator(types, ignoreVisibility, apply, consume, animate) {

        constructor(builder: Builder, @InsetsSide.Sides apply: Int) : this(
            types = builder.types,
            ignoreVisibility = builder.ignoreVisibility,
            apply = apply,
            consume = builder.consume,
            animate = builder.animate
        )

    }

    open class PaddingApplicator(
        types: Int,
        ignoreVisibility: Boolean = false,
        @InsetsSide.Sides apply: Int = InsetsSide.NONE,
        @InsetsSide.Sides consume: Int = InsetsSide.NONE,
        @InsetsSide.Sides animate: Int = InsetsSide.NONE
    ) : InsetApplicator(types, ignoreVisibility, apply, consume, animate) {

        constructor(builder: Builder, @InsetsSide.Sides apply: Int) : this(
            types = builder.types,
            ignoreVisibility = builder.ignoreVisibility,
            apply = apply,
            consume = builder.consume,
            animate = builder.animate
        )

    }

    class Builder(
        private val builder: InsetsDelegate.Builder,
        val types: Int
    ) {

        var ignoreVisibility: Boolean = false

        @InsetsSide.Sides
        var consume: Int = InsetsSide.NONE
            private set

        @InsetsSide.Sides
        var animate: Int = InsetsSide.NONE
            private set

        fun setIgnoreVisibility(flag: Boolean): Builder = apply {
            this.ignoreVisibility = flag
        }

        fun setConsume(@InsetsSide.Sides sides: Int): Builder = apply {
            this.consume = sides
        }

        fun setAnimate(@InsetsSide.Sides sides: Int): Builder = apply {
            this.animate = sides
        }

        fun build(applicator: InsetApplicator): InsetsDelegate.Builder {
            require(applicator.types != 0) {
                "The applicator must have an inset types defined"
            }
            require(
                applicator.apply != InsetsSide.NONE ||
                        applicator.consume != InsetsSide.NONE ||
                        applicator.animate != InsetsSide.NONE
            ) {
                "At least one side must be specified to apply, consume, or animate"
            }
            return builder.addApplicator(applicator)
        }

    }

}