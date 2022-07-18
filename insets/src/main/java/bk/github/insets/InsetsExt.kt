@file:Suppress("unused", "NOTHING_TO_INLINE")

package bk.github.insets

import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type

internal inline fun Insets.isEmpty(): Boolean = this == Insets.NONE

inline fun WindowInsetsCompat.getInsets(typeMask: Int, ignoreVisibility: Boolean) =
    if (ignoreVisibility) getInsetsIgnoringVisibility(typeMask) else getInsets(typeMask)

inline fun windowInsetsTypesOf(
    captionBar: Boolean = false,
    displayCutout: Boolean = false,
    ime: Boolean = false,
    mandatorySystemGestures: Boolean = false,
    navigationBars: Boolean = false,
    statusBars: Boolean = false,
    systemGestures: Boolean = false,
    tappableElement: Boolean = false
): Int {
    var type = 0
    if (captionBar) type = type or Type.captionBar()
    if (displayCutout) type = type or Type.displayCutout()
    if (ime) type = type or Type.ime()
    if (mandatorySystemGestures) type = type or Type.mandatorySystemGestures()
    if (navigationBars) type = type or Type.navigationBars()
    if (statusBars) type = type or Type.statusBars()
    if (systemGestures) type = type or Type.systemGestures()
    if (tappableElement) type = type or Type.tappableElement()
    return type
}

