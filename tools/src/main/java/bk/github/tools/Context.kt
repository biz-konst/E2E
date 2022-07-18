@file:Suppress("unused", "NOTHING_TO_INLINE")

package bk.github.tools

import android.content.Context
import android.util.TypedValue
import android.widget.Toast
import androidx.annotation.StringRes

inline fun Context.dip2px(inValue: Float): Float =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, inValue, resources.displayMetrics)

inline fun Context.dip2px(inValue: Int): Int = dip2px(inValue.toFloat()).toInt()

inline fun Context?.getString(@StringRes resId: Int?): String? =
    if (resId == null) null else this?.getString(resId)

inline fun Context?.toast(text: String, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, text, duration).show()

inline fun Context?.toast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, resId, duration).show()

