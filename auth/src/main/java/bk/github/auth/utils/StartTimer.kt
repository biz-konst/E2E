package bk.github.auth.utils

import bk.github.tools.tickerFlow
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private const val TIMER_INTERVAL_MS = 1_000L
private const val FOREVER_MS = 365 * 24 * 60 * 60 * 1_000L

fun startCountdownTimer(endTime: Long): Flow<Long> {
    val final = endTime - System.currentTimeMillis()

    if (final >= FOREVER_MS) {
        return flowOf(Long.MAX_VALUE)
    }

    return tickerFlow(TIMER_INTERVAL_MS, final)
        .map { final - it }
        .onCompletion { emit(0) }
}
