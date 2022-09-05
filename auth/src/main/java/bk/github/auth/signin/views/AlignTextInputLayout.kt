package bk.github.auth.signin.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.material.R
import com.google.android.material.textfield.TextInputLayout

class AlignTextInputLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.textInputStyle
) : TextInputLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val DEFAULT_HELPER_TEXT_TEXT_ALIGNMENT = View.TEXT_ALIGNMENT_VIEW_START
        private const val DEFAULT_ERROR_TEXT_ALIGNMENT = View.TEXT_ALIGNMENT_VIEW_START

        private val HELPER_TEXT_VIEW_ID = R.id.textinput_helper_text
        private val ERROR_VIEW_ID = R.id.textinput_error
    }

    var helperTextTextAlignment: Int = DEFAULT_HELPER_TEXT_TEXT_ALIGNMENT
        private set(value) {
            field = value
            setHelperTextTextAlignment()
        }

    var errorTextAlignment: Int = DEFAULT_ERROR_TEXT_ALIGNMENT
        private set(value) {
            field = value
            setErrorTextAlignment()
        }

    init {
        val a = this.context.obtainStyledAttributes(
            attrs, bk.github.auth.R.styleable.AlignTextInputLayout, defStyleAttr, 0
        )
        try {
            helperTextTextAlignment = a.getInt(
                bk.github.auth.R.styleable.AlignTextInputLayout_helperTextTextAlignment,
                DEFAULT_HELPER_TEXT_TEXT_ALIGNMENT
            )
            errorTextAlignment = a.getInt(
                bk.github.auth.R.styleable.AlignTextInputLayout_errorTextAlignment,
                DEFAULT_ERROR_TEXT_ALIGNMENT
            )
        } finally {
            a.recycle()
        }
    }

    override fun setHelperTextEnabled(enabled: Boolean) {
        super.setHelperTextEnabled(enabled)
        if (enabled) setHelperTextTextAlignment()
    }

    override fun setErrorEnabled(enabled: Boolean) {
        super.setErrorEnabled(enabled)
        if (enabled) setErrorTextAlignment()
    }

    private fun setHelperTextTextAlignment() {
        findViewById<AppCompatTextView>(HELPER_TEXT_VIEW_ID)?.let {
            if (it.textAlignment != helperTextTextAlignment) it.textAlignment =
                helperTextTextAlignment
        }
    }

    private fun setErrorTextAlignment() {
        findViewById<AppCompatTextView>(ERROR_VIEW_ID)?.let {
            if (it.textAlignment != errorTextAlignment) it.textAlignment = errorTextAlignment
        }
    }

}