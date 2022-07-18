@file:Suppress("unused", "NOTHING_TO_INLINE")

package bk.github.tools

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup

/**
 * Get view by coordinates in parent (ignoring z-oder)
 *
 * @param x x coordinate
 * @param y y coordinate
 * @param immediate don't continue searching inside nested view groups
 * @return found view or null if view is not found
 */
fun ViewGroup.findViewAt(x: Int, y: Int, immediate: Boolean = true): View? {
    for (i in 0 until childCount) {
        val child = getChildAt(i)

        if (!immediate && child is ViewGroup) {
            val nestedChild = child.findViewAt(x, y, false)

            if (nestedChild?.isShown == true) {
                return nestedChild
            }
        } else {
            val rect = Rect()

            child.getHitRect(rect)

            if (rect.contains(x, y)) {
                return child
            }
        }
    }
    return null
}