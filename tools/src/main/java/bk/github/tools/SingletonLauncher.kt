@file:Suppress("unused", "NOTHING_TO_INLINE")

package bk.github.tools

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class SingletonLauncher(val scope: CoroutineScope) : CoroutineScope by scope {

    private var job: Job? = null
    private val lock = Mutex()

    fun launch(
        start: CoroutineStart = CoroutineStart.DEFAULT,
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return scope.launch(start = CoroutineStart.UNDISPATCHED) {
            lock.withLock { job ?: launchInternal(start, context, block) }
        }
    }

    fun launchLatest(
        start: CoroutineStart = CoroutineStart.DEFAULT,
        context: CoroutineContext = EmptyCoroutineContext,
        key: Any? = null,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return scope.launch(start = CoroutineStart.UNDISPATCHED) {
            lock.withLock {
                job?.cancelAndJoin()
                launchInternal(start, context, block)
            }
        }
    }

    fun cancel() = job?.apply { cancel() }

    fun isActive() = job?.isActive == true

    private fun launchInternal(
        start: CoroutineStart,
        context: CoroutineContext,
        block: suspend CoroutineScope.() -> Unit
    ) {
        job = scope.launch(start = start, context = context, block = block)
    }

}

inline fun CoroutineScope.singletonLauncher() = SingletonLauncher(this)


