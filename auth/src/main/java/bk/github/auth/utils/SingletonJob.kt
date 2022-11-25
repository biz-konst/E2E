package bk.github.auth.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal class SingletonJob(var job: Job? = null) {
    val mutex = Mutex()
}

internal fun CoroutineScope.launchLatest(
    singletonJob: SingletonJob,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
): CoroutineScope = apply {
    launch(start = CoroutineStart.UNDISPATCHED) {
        singletonJob.mutex.withLock {
            singletonJob.job?.cancelAndJoin()
            singletonJob.job = launch(context, start, block)
        }
    }
}

