@file:Suppress("unused", "MemberVisibilityCanBePrivate", "NOTHING_TO_INLINE")

package bk.github.insets

data class Dimensions(
    val left: Int = 0,
    val top: Int = 0,
    val right: Int = 0,
    val bottom: Int = 0
) {

    companion object {
        val EMPTY = Dimensions()
    }

    @Suppress("SuspiciousEqualsCombination")
    inline fun isEmpty(): Boolean = this === EMPTY || this == EMPTY

    fun max(other: Dimensions) = Dimensions(
        left = if (left < other.left) other.left else left,
        top = if (top < other.top) other.top else top,
        right = if (right < other.right) other.right else right,
        bottom = if (bottom < other.bottom) other.bottom else bottom,
    )

    inline operator fun plus(added: Dimensions) = Dimensions(
        left = left + added.left,
        top = top + added.top,
        right = right + added.right,
        bottom = bottom + added.bottom
    )

}
