@file:Suppress("unused", "NOTHING_TO_INLINE")

package bk.github.tools

import android.os.SystemClock
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow

/**
 * Создает поток выдающий зачения через равные промежутки времени
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
 * Возвращает поток, который переключается на новый поток, создаваемый функцией преобразования,
 * каждый раз, когда исходный поток выдает значение. Когда исходный поток выдает новое значение,
 * предыдущий поток, созданный блоком преобразования, отменяется.
 */
fun <T, R> Flow<T>.switchMapLatest(block: (T) -> Flow<R>): Flow<R> = channelFlow {
    var job: Job? = null
    collect { value ->
        job?.cancelAndJoin()
        job = launch(start = CoroutineStart.UNDISPATCHED) { block(value).collect { send(it) } }
    }
}

///**
// * Терминальный оператор потока с учетом жизненного цикла
// */
//inline fun <T> Flow<T>.collectOnLifecycle(
//    lifecycleOwner: LifecycleOwner,
//    state: Lifecycle.State = Lifecycle.State.STARTED,
//    noinline block: (T) -> Unit
//) = lifecycleOwner.lifecycleScope.launch {
//    lifecycleOwner.repeatOnLifecycle(state) { collect(block) }
//}
//
//fun <T> MutableStateFlow<T>.launchWhileSubscribed(
//    scope: CoroutineScope,
//    once: Boolean = false,
//    block: suspend CoroutineScope.() -> Unit
//): MutableStateFlow<T> {
//    (this as MutableSharedFlow<*>).launchWhileSubscribed(scope, once, block)
//    return this
//}
//
//fun <T> MutableSharedFlow<T>.launchWhileSubscribed(
//    scope: CoroutineScope,
//    once: Boolean = false,
//    block: suspend CoroutineScope.() -> Unit
//): MutableSharedFlow<T> {
//    scope.launch(start = CoroutineStart.UNDISPATCHED) {
//        var launchJob: Job? = null
//        subscriptionCount
//            .collect { count ->
//                if (count == 0) {
//                    launchJob?.cancel()
//                } else if (launchJob == null || !once) {
//                    if (launchJob?.isActive != true) {
//                        launchJob = scope.launch(block = block)
//                    }
//                }
//            }
//    }
//    return this
//}
