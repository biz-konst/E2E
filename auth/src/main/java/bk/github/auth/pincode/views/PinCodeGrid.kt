package bk.github.auth.pincode.views

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.text.*
import android.text.method.PasswordTransformationMethod
import android.text.method.TransformationMethod
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Checkable
import android.widget.CheckedTextView
import android.widget.GridLayout
import android.widget.TextView
import androidx.core.os.ParcelCompat
import androidx.customview.view.AbsSavedState
import bk.github.auth.R
import bk.github.tools.findViewAt
import bk.github.tools.inflater
import com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap

@Suppress("MemberVisibilityCanBePrivate", "unused")
class PinCodeGrid @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : GridLayout(wrap(context, attrs, defStyleAttr, defStyleRes), attrs, defStyleAttr, defStyleRes) {

    companion object {
        private val ERROR_STATE = intArrayOf(R.attr.failed)
    }

    fun interface OnTextChangedListener {
        fun onPinCodeTextChanged(view: View, text: String)
    }

    /**
     * Текущий пин-код
     */
    var code: String
        get() = editable.toString()
        set(value) = setCodeInternal(value)

    /**
     * Максимальная длина пин-кода
     */
    val length: Int get() = slotViews.size

    /**
     * Признак неверного пин-кода для отображения состояния ошибки
     */
    var failed: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                refreshDrawableState()
            }
        }

    /**
     * Слушатель измнения пин-сода
     */
    @JvmField
    var onTextChangedListener: OnTextChangedListener? = null

    private var editable: Editable = SpannableStringBuilder().also {
        it.setSpan(PinWatcher(), 0, it.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
    }

    private val slotViews = arrayListOf<View>()

    private var transformer: TransformationMethod? = null
    private var transformed: CharSequence = editable

    init {
        val a = this.context.obtainStyledAttributes(
            attrs, R.styleable.PinCodeGrid, defStyleAttr, defStyleRes
        )
        try {
            setPasswordMode(a.getBoolean(R.styleable.PinCodeGrid_passwordMode, true))
            failed = a.getBoolean(R.styleable.PinCodeGrid_failed, false)
        } finally {
            a.recycle()
        }
    }

    override fun onViewAdded(child: View) {
        super.onViewAdded(child)
        if (child is TextView || child is Checkable) {
            slotViews.add(child)
            updateSlot(child, transformed.getOrNull(length - 1)?.toString())
        }
    }

    override fun onViewRemoved(child: View) {
        super.onViewRemoved(child)
        if (slotViews.remove(child)) {
            if (editable.length > length) {
                editable.delete(length, editable.length)
            }
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val handled = super.onInterceptTouchEvent(ev)
        // prevent click event for slot view
        if (!handled) {
            if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
                val x = ev.x.toInt()
                val y = ev.y.toInt()
                return slotViews.contains(findViewAt(x, y))
            }
        }
        return handled
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        return if (failed) {
            mergeDrawableStates(
                super.onCreateDrawableState(extraSpace + ERROR_STATE.size), ERROR_STATE
            )
        } else {
            super.onCreateDrawableState(extraSpace)
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is PinCodeSavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(state.superState)
        setCodeInternal(state.code ?: "")
        failed = state.failed
    }

    override fun onSaveInstanceState(): Parcelable {
        return PinCodeSavedState(super.onSaveInstanceState()).apply {
            code = this@PinCodeGrid.code
            failed = this@PinCodeGrid.failed
        }
    }

    /**
     * Добавить символ в конец текущего пин-кода
     * @param char добавляемый символ
     * @return true - если символ был добавлен
     */
    fun append(char: Char): Boolean {
        if (code.length >= length) return false

        editable.append(char)
        return true
    }

    /**
     * Удалить символ из указанной позиции пин-кода
     * @param index позиция удаляемого символа
     */
    fun remove(index: Int) {
        if (index < 0 || index >= code.length) return

        editable.delete(index.coerceAtMost(length - 1), index + 1)
    }

    /**
     * Очистить пин-код
     */
    fun clear() {
        editable.clear()
    }

    /**
     * Установить режим пароля.
     * В режиме пароля введенные символы экранируются заполнителем
     */
    fun setPasswordMode(mode: Boolean) {
        if (mode == transformer is PasswordTransformationMethod) return

        transformer?.let { editable.removeSpan(it) }

        if (mode) {
            transformer = PasswordTransformationMethod.getInstance().also {
                transformed = it.getTransformation(editable, this)
                editable.setSpan(it, 0, editable.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
            }
        } else {
            transformer = null
            transformed = editable
        }

        updateSlots()
    }

    /**
     * Установить слушатель изменения пин-кода
     *
     * @param listener новый слушатель изменения пин-кода
     */
    fun setOnTextChangedListener(listener: OnTextChangedListener? = null) {
        onTextChangedListener = listener
    }

    private fun updateSlots(start: Int = 0, end: Int = length) {
        require(end <= length) { "Illegal upper bound of range $end" }
        require(start in 0..end) { "Illegal lower bound of range $start" }
        for (i in start until end) {
            updateSlot(slotViews[i], transformed.getOrNull(i)?.toString())
        }
    }

    private fun updateSlot(view: View, text: String?) {
        if (view is Checkable) {
            if (view is CheckedTextView && view.checkMarkDrawable == null) {
                view.text = text
                return
            }
            view.isChecked = text != null
        } else if (view is TextView) {
            view.text = text
        }
    }

    private fun setCodeInternal(value: String) {
        editable.replace(0, editable.length, value)
    }

    private inner class PinWatcher : TextWatcher, SpanWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit

        override fun afterTextChanged(s: Editable) {
            updateSlots()
            onTextChangedListener?.onPinCodeTextChanged(this@PinCodeGrid, s.toString())
        }

        override fun onSpanAdded(text: Spannable, what: Any, start: Int, end: Int) = Unit

        override fun onSpanRemoved(text: Spannable, what: Any, start: Int, end: Int) {
            updateSlots(start, end)
        }

        override fun onSpanChanged(
            text: Spannable, what: Any, ostart: Int, oend: Int, nstart: Int, nend: Int
        ) = Unit
    }

    private class PinCodeSavedState(superState: Parcelable?) :
        AbsSavedState(superState ?: EMPTY_STATE) {

        var code: String? = null
        var failed: Boolean = false

        @JvmOverloads
        constructor(source: Parcel, loader: ClassLoader? = null) : this(
            source.readParcelable<Parcelable>(loader)
        ) {
            code = source.readString()
            failed = ParcelCompat.readBoolean(source)
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            super.writeToParcel(parcel, flags)
            parcel.writeString(code)
            ParcelCompat.writeBoolean(parcel, failed)
        }

        companion object CREATOR : Parcelable.ClassLoaderCreator<PinCodeSavedState> {

            override fun createFromParcel(source: Parcel): PinCodeSavedState =
                PinCodeSavedState(source)

            override fun createFromParcel(source: Parcel, loader: ClassLoader): PinCodeSavedState =
                PinCodeSavedState(source, loader)

            override fun newArray(size: Int): Array<PinCodeSavedState?> = arrayOfNulls(size)

        }

    }

}

/**
 * Добавить/удалить вложенные вью в соответствии с указанной длиной
 *
 * @param length требуемая длина
 * @param viewId ид макета для добавляемых вью
 */
fun PinCodeGrid.applyLength(length: Int, viewId: Int) {
    require(length >= 0) {
        "The pin code length cannot be less than zero"
    }
    repeat(childCount - length) { removeView(getChildAt(0)) }
    val inflater = this.inflater
    repeat(length - childCount) {
        inflater.inflate(viewId, this, true)
    }
}

/**
 * Добавить символ в конец текущего пин-кода или в начало, если пи-код уже полон
 * @param char добавляемый символ
 * @return true - если символ был добавлен
 */
fun PinCodeGrid.forceAppend(char: Char): Boolean {
    if (append(char)) return true

    clear()
    return append(char)
}

/**
 * Удалить символ из последней позиции пин-кода
 */
fun PinCodeGrid.removeLast() {
    remove(code.lastIndex)
}