package bk.github.auth.signin.views

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.annotation.IntDef
import androidx.annotation.Px
import androidx.core.os.ParcelCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.customview.view.AbsSavedState
import bk.github.auth.R
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlin.math.max

@Suppress("MemberVisibilityCanBePrivate")
class ProgressButtonLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    companion object {
        const val PROGRESS_GRAVITY_CENTER = 0
        const val PROGRESS_GRAVITY_LEFT = 1
        const val PROGRESS_GRAVITY_RIGHT = 2

        const val PROGRESS_GRAVITY_START = -PROGRESS_GRAVITY_LEFT
        const val PROGRESS_GRAVITY_END = -PROGRESS_GRAVITY_RIGHT

        private const val PROGRESS_INSET_LEFT_DEFAULT = 6f   // 6dp
        private const val PROGRESS_INSET_RIGHT_DEFAULT = 6f   // 6dp
        private const val PROGRESS_INSET_TOP_DEFAULT = -2f   // -2dp
        private const val PROGRESS_INSET_BOTTOM_DEFAULT = -2f   // -2dp
        private const val PROGRESS_GRAVITY_DEFAULT = PROGRESS_GRAVITY_END
    }

    @IntDef(
        PROGRESS_GRAVITY_LEFT,
        PROGRESS_GRAVITY_RIGHT,
        PROGRESS_GRAVITY_CENTER,
        PROGRESS_GRAVITY_START,
        PROGRESS_GRAVITY_END,
    )
    @Retention(AnnotationRetention.SOURCE)
    @Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
    annotation class ProgressGravity

    @Px
    var pbInsetLeft: Int = getDimensionPixelOffset(PROGRESS_INSET_LEFT_DEFAULT)
        set(value) {
            if (field != value) {
                field = value
                pbInsetsChanged()
            }
        }

    @Px
    var pbInsetTop: Int = getDimensionPixelOffset(PROGRESS_INSET_TOP_DEFAULT)
        set(value) {
            if (field != value) {
                field = value
                pbInsetsChanged()
            }
        }

    @Px
    var pbInsetRight: Int = getDimensionPixelOffset(PROGRESS_INSET_RIGHT_DEFAULT)
        set(value) {
            if (field != value) {
                field = value
                pbInsetsChanged()
            }
        }

    @Px
    var pbInsetBottom: Int = getDimensionPixelOffset(PROGRESS_INSET_BOTTOM_DEFAULT)
        set(value) {
            if (field != value) {
                field = value
                pbInsetsChanged()
            }
        }

    @ProgressGravity
    var pbGravity: Int = PROGRESS_GRAVITY_DEFAULT
        set(value) {
            if (field != value) {
                field = value
                pbGravityChanged()
            }
        }

    var button: Button? = null
        private set

    var progressBar: ProgressBar? = null
        private set

    var isProgressShown = false
        set(value) {
            if (field != value) {
                field = value
                showProgress(value)
            }
        }

    private var buttonPadding = 0
    private var absProgressGravity = pbGravity

    init {
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.ProgressButtonLayout, defStyleAttr, defStyleRes
        )
        pbInsetLeft =
            a.getDimensionPixelOffset(R.styleable.ProgressButtonLayout_pbInsetLeft, pbInsetLeft)
        pbInsetTop =
            a.getDimensionPixelOffset(R.styleable.ProgressButtonLayout_pbInsetTop, pbInsetTop)
        pbInsetRight =
            a.getDimensionPixelOffset(R.styleable.ProgressButtonLayout_pbInsetRight, pbInsetRight)
        pbInsetBottom =
            a.getDimensionPixelOffset(R.styleable.ProgressButtonLayout_pbInsetBottom, pbInsetBottom)
        pbGravity = a.getInt(R.styleable.ProgressButtonLayout_pbGravity, pbGravity)
        a.recycle()
    }

    override fun addView(child: View, index: Int, params: LayoutParams) {
        super.addView(child, index, params)
        when (child) {
            is Button -> setButton(child)
            is ProgressBar -> setProgressBar(child)
            else -> throw IllegalArgumentException("You can only add a button or a progress bar")
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        resolveProgressBarVisibility()

        var maxWidth = 0
        var maxHeight = 0
        var childState = 0

        button?.apply {
            if (visibility != GONE) {
                measureChild(this, widthMeasureSpec, heightMeasureSpec)
                progressBar?.let { pb ->
                    if (pb.isVisible) {
                        val size =
                            measuredHeight - paddingTop - paddingBottom - pbInsetTop - pbInsetBottom
                        if (pb is CircularProgressIndicator) {
                            pb.indicatorSize = size
                        }
                        val measuredSize = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY)
                        pb.measure(measuredSize, measuredSize)

                        val padding = size + pbInsetLeft + pbInsetRight
                        setButtonPadding(padding.coerceAtLeast(buttonPadding))
                        measureChild(this, widthMeasureSpec, heightMeasureSpec)
                    }
                }

                maxWidth = measuredWidth
                maxHeight = measuredHeight
                childState = measuredState
            }
        }

        maxHeight = max(maxHeight + paddingLeft + paddingRight, suggestedMinimumHeight)
        maxWidth = max(maxWidth + paddingTop + paddingBottom, suggestedMinimumWidth)

        setMeasuredDimension(
            resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
            resolveSizeAndState(
                maxHeight, heightMeasureSpec, childState shl MEASURED_HEIGHT_STATE_SHIFT
            )
        )
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        layoutChildren(right - left, bottom - top)
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val state = button?.drawableState ?: return super.onCreateDrawableState(extraSpace)
        if (extraSpace == 0) return state

        return IntArray(state.size + extraSpace).apply {
            System.arraycopy(state, 0, this, 0, state.size)
        }
    }

    override fun childDrawableStateChanged(child: View?) {
        super.childDrawableStateChanged(child)
        if (child == button) progressBar?.refreshDrawableState()
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            return super.onRestoreInstanceState(state)
        }
        super.onRestoreInstanceState(state.superState)
        isProgressShown = state.isProgressShown
    }

    override fun onSaveInstanceState(): Parcelable {
        return SavedState(super.onSaveInstanceState()).apply {
            isProgressShown = this@ProgressButtonLayout.isProgressShown
        }
    }

    fun updateChildParams() {
        buttonPadding = 0
        absProgressGravity = absProgressGravity(pbGravity)
        button?.apply {
            when (absProgressGravity) {
                PROGRESS_GRAVITY_LEFT -> buttonPadding = paddingLeft
                PROGRESS_GRAVITY_RIGHT -> buttonPadding = paddingRight
            }
        }
    }

    private fun setButton(value: Button) {
        require(button == null) { "We already have a Button, can only have one" }
        button = value
    }

    private fun setProgressBar(value: ProgressBar) {
        require(progressBar == null) { "We already have a ProgressBar, can only have one" }
        progressBar = value.apply { isDuplicateParentStateEnabled = true }
        updateChildParams()
        showProgress(isProgressShown)
    }

    private fun resolveProgressBarVisibility() {
        progressBar?.apply {
            val visible = isProgressShown && (button?.isVisible == true)
            if (isVisible != visible) isVisible = visible
            if (visible) {
                button?.let { if (elevation < it.elevation) elevation = it.elevation + 0.01f }
            }
        }
    }

    private fun layoutChildren(parentWidth: Int, parentHeight: Int) {
        button?.apply {
            layout(0, 0, parentWidth, parentHeight)

            progressBar?.let { pb ->
                if (pb.isVisible) {
                    val size = pb.measuredHeight
                    val offset = when (absProgressGravity) {
                        PROGRESS_GRAVITY_RIGHT -> {
                            right - pbInsetRight - size
                        }
                        PROGRESS_GRAVITY_CENTER -> {
                            left + (width - size) / 2 + pbInsetLeft - pbInsetRight
                        }
                        else -> {
                            left + pbInsetLeft
                        }
                    }
                    var pbTop = top + paddingTop + pbInsetTop
                    if (pb is CircularProgressIndicator) {
                        pbTop -= pb.indicatorInset
                    }
                    pb.layout(offset, pbTop, offset + size, pbTop + size)
                }
            }
        }
    }

    private fun showProgress(visible: Boolean) {
        progressBar?.let { pb ->
            pb.isVisible = visible
            button?.let {
                if (visible) {
                    updateChildParams()
                    bringChildToFront(pb)
                } else {
                    setButtonPadding(buttonPadding)
                }
            }
        }
    }

    private fun absProgressGravity(@ProgressGravity gravity: Int) =
        when (gravity) {
            PROGRESS_GRAVITY_START ->
                if (layoutDirection == LAYOUT_DIRECTION_RTL) PROGRESS_GRAVITY_RIGHT else PROGRESS_GRAVITY_LEFT
            PROGRESS_GRAVITY_END ->
                if (layoutDirection == LAYOUT_DIRECTION_RTL) PROGRESS_GRAVITY_LEFT else PROGRESS_GRAVITY_RIGHT
            else -> gravity
        }

    private fun setButtonPadding(padding: Int) {
        when (absProgressGravity) {
            PROGRESS_GRAVITY_LEFT -> button?.updatePadding(left = padding)
            PROGRESS_GRAVITY_RIGHT -> button?.updatePadding(right = padding)
        }
    }

    private fun pbInsetsChanged() {
        progressBar?.requestLayout()
    }

    private fun pbGravityChanged() {
        progressBar?.requestLayout()
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun getDimensionPixelOffset(dip: Float) =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, resources.displayMetrics)
            .toInt()

    private class SavedState(superState: Parcelable?) : AbsSavedState(superState ?: EMPTY_STATE) {

        var isProgressShown = false

        @JvmOverloads
        constructor(parcel: Parcel, loader: ClassLoader? = null) : this(
            parcel.readParcelable<Parcelable>(loader)
        ) {
            isProgressShown = ParcelCompat.readBoolean(parcel)
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            super.writeToParcel(parcel, flags)
            ParcelCompat.writeBoolean(parcel, isProgressShown)
        }

        companion object CREATOR : Parcelable.ClassLoaderCreator<SavedState> {

            override fun createFromParcel(source: Parcel): SavedState =
                SavedState(source)

            override fun createFromParcel(source: Parcel, loader: ClassLoader): SavedState =
                SavedState(source, loader)

            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)

        }

    }

}