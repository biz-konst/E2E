package bk.github.insets

internal class InsetsAnimateInfo(
    val marginsTypes: Int = 0,
    @InsetsSide.Sides val marginsSides: Int = InsetsSide.NONE,
    val paddingsTypes: Int = 0,
    @InsetsSide.Sides val paddingsSides: Int = InsetsSide.NONE
)