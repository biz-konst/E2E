package bk.github.auth.utils

import android.text.SpanWatcher
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.util.Log
import android.widget.TextView

object TimeoutShowHelper {

    fun showTimeout(view: TextView, timeoutText: String) {
        val viewText = SpannableStringBuilder(view.text)
        var start = viewText.getSpanStart(this)
        if (start >= 0) {
            val end = viewText.getSpanEnd(this)
            viewText.replace(start, end, timeoutText)
            view.text = viewText
        } else if (timeoutText.isNotEmpty()) {
            start = viewText.length
            viewText.append(timeoutText)
            viewText.setSpan(this, start, viewText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            view.text = viewText
        }
    }

}