@file:Suppress("unused", "NOTHING_TO_INLINE", "MemberVisibilityCanBePrivate")

package bk.github.tools

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SingletonLauncher {

    private var job: Job? = null
    private val lock = Mutex()

    fun launch(
        scope: CoroutineScope,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        latest: Boolean = true,
        block: suspend CoroutineScope.() -> Unit
    ) = scope.launch(start = CoroutineStart.UNDISPATCHED) {
        lock.withLock {
            if (latest || job == null) {
                job?.cancelAndJoin()
                job = scope.launch(start = start, block = block)
            }
        }
    }

    fun cancel() = job?.cancel()

    fun isActive() = job?.isActive == true

    suspend fun join() = job?.join()

}

