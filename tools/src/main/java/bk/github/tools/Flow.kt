@file:Suppress("unused", "NOTHING_TO_INLINE")

package bk.github.tools

import android.os.SystemClock
import androidx.lifecycle.*
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

//inline fun <T> MutableStateFlow<T>.update(block: (T) -> T): Flow<T> = apply {
//    val new = block(value)
//    if (new != value) value = new
//}
//
/**
 * Поток с эмиссией через равные промежутки времени
 */
fun tickerFlow(interval: Long, finalInterval: Long = Long.MAX_VALUE): Flow<Long> {
    require(interval > 0) { "Interval cannot be empty" }
    return flow {
        var elapsedTime = 0L
        val startTime = SystemClock.elapsedRealtime()
        while (currentCoroutineContext().isActive && elapsedTime < finalInterval) {
            emit(elapsedTime)
            val exactInterval = interval - elapsedTime % interval
            delay(exactInterval.coerceAtMost(finalInterval - elapsedTime))
            elapsedTime = SystemClock.elapsedRealtime() - startTime
        }
    }
}

/**
 * Терминальный оператор потока с учетом жизненного цикла
 */
inline fun <T> Flow<T>.collectOnLifecycle(
    lifecycleOwner: LifecycleOwner,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    noinline block: (T) -> Unit
) = lifecycleOwner.lifecycleScope.launch {
    lifecycleOwner.repeatOnLifecycle(state) { collect(block) }
}

