@file:Suppress("unused", "NOTHING_TO_INLINE")

package bk.github.tools

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.core.content.ContextCompat

@Suppress("DEPRECATION")
fun WindowManager.windowSize(size: Point) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        currentWindowMetrics.bounds.let {
            size.x = it.width()
            size.y = it.height()
        }
    } else {
        val metrics = DisplayMetrics()
        defaultDisplay.getRealMetrics(metrics)
        size.x = metrics.widthPixels
        size.y = metrics.heightPixels
    }
}

val WindowManager.windowSize: Point get() = Point().also { windowSize(it) }

val Context.windowManager: WindowManager?
    get() = ContextCompat.getSystemService(this, WindowManager::class.java)

