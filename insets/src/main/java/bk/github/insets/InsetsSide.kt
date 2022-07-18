package bk.github.insets

import androidx.annotation.IntDef

object InsetsSide {

    @IntDef(value = [NONE, LEFT, TOP, RIGHT, BOTTOM])
    @Retention(AnnotationRetention.SOURCE)
    @Target(
        AnnotationTarget.VALUE_PARAMETER,
        AnnotationTarget.PROPERTY,
        AnnotationTarget.LOCAL_VARIABLE,
        AnnotationTarget.FIELD,
        AnnotationTarget.FUNCTION
    )
    annotation class Sides

    const val NONE = 0
    const val LEFT = 1
    const val TOP = 2
    const val RIGHT = 4
    const val BOTTOM = 8

    @InsetsSide.Sides
    fun of(
        left: Boolean = false,
        top: Boolean = false,
        right: Boolean = false,
        bottom: Boolean = false,
        horizontal: Boolean = false,
        vertical: Boolean = false
    ): Int {
        var sides = NONE
        if (left || horizontal) sides += LEFT
        if (top || vertical) sides += TOP
        if (right || horizontal) sides += RIGHT
        if (bottom || vertical) sides += BOTTOM
        return sides
    }

}


