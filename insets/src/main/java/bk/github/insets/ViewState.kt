@file:Suppress("unused", "NOTHING_TO_INLINE")

package bk.github.insets

import android.view.View
import android.view.ViewGroup

data class ViewState(
    val paddings: Dimensions,
    val margins: Dimensions
) {

    constructor(view: View) : this(
        paddings = view.paddingsDimensions,
        margins = view.marginsDimensions
    )

}

internal inline val View.paddingsDimensions: Dimensions
    get() = Dimensions(paddingLeft, paddingTop, paddingRight, paddingBottom)

internal inline val View.marginsDimensions: Dimensions
    get() = if (layoutParams !is ViewGroup.MarginLayoutParams)
        Dimensions.EMPTY
    else with(layoutParams as ViewGroup.MarginLayoutParams) {
        Dimensions(leftMargin, topMargin, rightMargin, bottomMargin)
    }

internal inline val View.viewState: ViewState
    get() = getTag(R.id.tag_insets_view_state) as? ViewState
        ?: updateViewState().let { getTag(R.id.tag_insets_view_state) as ViewState }

internal fun View.updateViewState() {
    setTag(R.id.tag_insets_view_state, ViewState(this))
}