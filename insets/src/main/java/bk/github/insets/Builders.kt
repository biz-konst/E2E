@file:Suppress("unused", "MemberVisibilityCanBePrivate", "NOTHING_TO_INLINE")

package bk.github.insets

import android.annotation.SuppressLint

inline fun InsetsDelegate.Builder.insets(
    captionBar: Boolean = false,
    displayCutout: Boolean = false,
    ime: Boolean = false,
    mandatorySystemGestures: Boolean = false,
    navigationBars: Boolean = false,
    statusBars: Boolean = false,
    systemGestures: Boolean = false,
    tappableElement: Boolean = false
): InsetApplicator.Builder {
    val types = windowInsetsTypesOf(
        captionBar,
        displayCutout,
        ime,
        mandatorySystemGestures,
        navigationBars,
        statusBars,
        systemGestures,
        tappableElement
    )
    return insets(types)
}

inline fun InsetsDelegate.Builder.insets(types: Int): InsetApplicator.Builder =
    InsetApplicator.Builder(this, types)

inline fun InsetApplicator.Builder.consume(
    left: Boolean = false,
    top: Boolean = false,
    right: Boolean = false,
    bottom: Boolean = false,
    horizontal: Boolean = false,
    vertical: Boolean = false,
): InsetApplicator.Builder =
    setConsume(InsetsSide.of(left, top, right, bottom, horizontal, vertical))

inline fun InsetApplicator.Builder.animate(
    left: Boolean = false,
    top: Boolean = false,
    right: Boolean = false,
    bottom: Boolean = false,
    horizontal: Boolean = false,
    vertical: Boolean = false,
): InsetApplicator.Builder =
    setAnimate(InsetsSide.of(left, top, right, bottom, horizontal, vertical))

@SuppressLint("WrongConstant")
inline fun InsetApplicator.Builder.padding(
    left: Boolean = false,
    top: Boolean = false,
    right: Boolean = false,
    bottom: Boolean = false,
    horizontal: Boolean = false,
    vertical: Boolean = false,
    ignoreVisibility: Boolean = false,
    consume: Boolean = false,
    animate: Boolean = false
): InsetsDelegate.Builder {
    val sides = InsetsSide.of(left, top, right, bottom, horizontal, vertical)
    return build(
        InsetApplicator.PaddingApplicator(
            types = types,
            ignoreVisibility = ignoreVisibility || this.ignoreVisibility,
            apply = sides,
            consume = if (consume) this.consume or sides else this.consume,
            animate = if (animate) this.animate or sides else this.animate
        )
    )
}

@SuppressLint("WrongConstant")
inline fun InsetApplicator.Builder.margin(
    left: Boolean = false,
    top: Boolean = false,
    right: Boolean = false,
    bottom: Boolean = false,
    horizontal: Boolean = false,
    vertical: Boolean = false,
    ignoreVisibility: Boolean = false,
    consume: Boolean = false,
    animate: Boolean = false
): InsetsDelegate.Builder {
    val sides = InsetsSide.of(left, top, right, bottom, horizontal, vertical)
    return build(
        InsetApplicator.MarginApplicator(
            types = types,
            ignoreVisibility = ignoreVisibility || this.ignoreVisibility,
            apply = sides,
            consume = if (consume) this.consume or sides else this.consume,
            animate = if (animate) this.animate or sides else this.animate
        )
    )
}



