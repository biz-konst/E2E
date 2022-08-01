package bk.github.auth.pincode.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bk.github.auth.pincode.PinCodeFeatureConfig
import bk.github.auth.pincode.data.PinCodeManager
import bk.github.auth.pincode.data.model.PinCodeState
import bk.github.auth.pincode.data.model.PinCodeValue
import bk.github.auth.pincode.ui.PinCodeViewModel.ModelState.PerformStatus
import bk.github.auth.pincode.ui.PinCodeViewModel.UiState.PinCodeStatus
import bk.github.auth.pincode.ui.PinCodeViewModel.UiState.RequestStatus
import bk.github.tools.tickerFlow
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@Suppress("unused")
class PinCodeViewModel(
    private val manager: PinCodeManager,
    internal val config: PinCodeFeatureConfig
) : ViewModel() {

    companion object {
        private const val SECOND_MS = 1000L
        private const val FOREVER_MS = 31_536_000_000L

        const val TAG = "Auth.PinCodeViewModel"
    }

    @JvmField
    var queryDelayTimerInterval = 1 * SECOND_MS

    private val requestResult = MutableSharedFlow<PinCodeState>()
    private val pinCodeState = merge(manager.observePinCodeState(), requestResult)
        .stateIn(
            scope = viewModelScope + Dispatchers.Default,
            started = SharingStarted.Eagerly,
            initialValue = PinCodeState.EMPTY
        )

    @ExperimentalCoroutinesApi
    private val queryTimeoutTimer = pinCodeState
        .map { it.lastRequestTime + it.requestTimeout }.distinctUntilChanged()
        .flatMapLatest { endTime -> startTimer(endTime) }

    @ExperimentalCoroutinesApi
    private val codeLifetimeTimer = pinCodeState
        .map { it.expirationTime }.distinctUntilChanged()
        .flatMapLatest { endTime -> startTimer(endTime) }

    private val uiEvent = MutableSharedFlow<UiEvent>()
    private val modelState = MutableStateFlow(ModelState.EMPTY)

    @ExperimentalCoroutinesApi
    private val eventReducer = uiEvent
        .transformLatest<UiEvent, Unit> { event ->
            when (event) {
                is UiEvent.CheckPinCode -> {
                    val error = verifyPinCode(event.code)
                    if (error != null) {
                        applyModelState { pinCodeFailed(error) }
                    } else if (codeIsFull(event.code)) {
                        applyModelState { pinCodeChecking() }
                        applyModelState {
                            acceptPinCode(event.code).fold(
                                onFailure = { pinCodeFailed(it) },
                                onSuccess = { pinCodeAccepted() }
                            )
                        }
                    }
                }
                is UiEvent.QueryPinCode -> {
                    applyModelState { requestPerforming() }
                    applyModelState {
                        requestPinCode().fold(
                            onFailure = { requestFailed(it) },
                            onSuccess = {
                                requestResult.emit(it)
                                requestSuccess()
                            }
                        )
                    }
                }
                else -> {}
            }
        }
        .onStart { emit(Unit) }

    @ExperimentalCoroutinesApi
    val uiState =
        combine(
            pinCodeState,
            modelState,
            codeLifetimeTimer,
            queryTimeoutTimer,
            eventReducer
        ) { p, a, l, t, _ ->
            UiState(
                pinCodeState = UiState.PinCodeState(
                    status = a.checkPinCodeStatus.pinCodeStatus,
                    length = p.length,
                    availableAttempts = availableAttempts(p),
                    failure = a.checkPinCodeStatus.failure,
                ),
                requestState = UiState.RequestState(
                    status = a.queryPinCodeStatus.requestStatus,
                    failure = a.queryPinCodeStatus.failure,
                ),
                lifetime = l,
                attemptNumber = p.attemptsSpent,
                timeout = t,
            )
        }.flowOn(
            Dispatchers.Default
        ).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(
                stopTimeoutMillis = config.stopUiStateFlowTimeoutMs
            ),
            initialValue = UiState.EMPTY
        )

    fun checkPinCode(code: String): Boolean {
        viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            uiEvent.emit(UiEvent.CheckPinCode(code))
        }
        return codeIsFull(code)
    }

    fun queryPinCode() {
        viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            uiEvent.emit(UiEvent.QueryPinCode)
        }
    }

    fun resetPinCodeState() {
        applyModelState { cleanPinCodeResult() }
    }

    fun resetRequestState() {
        applyModelState { cleanPinCodeResult() }
    }

    private fun codeIsFull(code: String) = code.length == pinCodeState.value.length

    private fun emitEvent(event: UiEvent) = uiEvent.tryEmit(event)

    private suspend fun verifyPinCode(code: String): Throwable? {
        try {
            manager.verifyPinCode(code)?.let {
                return Exception(it)
            }
        } finally {
        }
        return null
    }

    private suspend fun acceptPinCode(code: String): Result<*> = runCatching {
        manager.acceptPinCode(PinCodeValue(id = pinCodeState.value.id, value = code)).getOrThrow()
    }

    private suspend fun requestPinCode(): Result<PinCodeState> = runCatching {
        manager.requestPinCode(id = pinCodeState.value.id).getOrThrow()
    }

    private fun availableAttempts(state: PinCodeState): Int =
        if (state.length == 0) 0 else state.numberOfAttempts

    private fun startTimer(endTime: Long): Flow<Long> {
        val final = endTime - System.currentTimeMillis()

        if (final >= FOREVER_MS) {
            return flowOf(Long.MAX_VALUE)
        }

        return tickerFlow(queryDelayTimerInterval, final)
            .map { final - it }
            .onCompletion { emit(0) }
    }

    data class UiState(
        val pinCodeState: PinCodeState,
        val requestState: RequestState,
        val lifetime: Long = 0,
        val attemptNumber: Int = 0,
        val timeout: Long = 0,
    ) {
        enum class PinCodeStatus { Input, Checking, Accepted }

        data class PinCodeState(
            val status: PinCodeStatus,
            val length: Int = 0,
            val availableAttempts: Int = 0,
            val failure: Throwable? = null
        )

        enum class RequestStatus { Ready, Performing, Done }

        data class RequestState(
            val status: RequestStatus,
            val failure: Throwable? = null
        )

        companion object {
            val EMPTY = UiState(
                pinCodeState = PinCodeState(PinCodeStatus.Input),
                requestState = RequestState(RequestStatus.Ready),
            )
        }
    }

    private sealed interface UiEvent {
        @JvmInline
        value class CheckPinCode(val code: String) : UiEvent
        object QueryPinCode : UiEvent
        object CheckPinCodeResultHandled : UiEvent
        object QueryPinCodeResultHandled : UiEvent
    }

    private data class ModelState(
        val checkPinCodeStatus: PerformStatus,
        val queryPinCodeStatus: PerformStatus,
    ) {
        sealed interface PerformStatus {
            object None : PerformStatus
            object Performing : PerformStatus
            open class Done(val e: Throwable? = null) : PerformStatus
        }

        companion object {
            val EMPTY = ModelState(
                checkPinCodeStatus = PerformStatus.None,
                queryPinCodeStatus = PerformStatus.None,
            )
        }
    }

    private inline fun applyModelState(action: ModelState.() -> ModelState) {
        modelState.update(action)
    }

    private inline val PerformStatus.failure
        get() = (this as? PerformStatus.Done)?.e

    private inline val PerformStatus.pinCodeStatus
        get() = when {
            this is PerformStatus.Performing -> PinCodeStatus.Checking
            this is PerformStatus.Done && e == null -> PinCodeStatus.Accepted
            else -> PinCodeStatus.Input
        }

    private inline val PerformStatus.requestStatus
        get() = when (this) {
            is PerformStatus.Performing -> RequestStatus.Performing
            is PerformStatus.Done -> RequestStatus.Done
            else -> RequestStatus.Ready
        }

    private fun ModelState.pinCodeChecking() = copy(checkPinCodeStatus = PerformStatus.Performing)

    private fun ModelState.pinCodeAccepted() = copy(checkPinCodeStatus = PerformStatus.Done())

    private fun ModelState.pinCodeFailed(e: Throwable) =
        if (e is CancellationException) {
            copy(checkPinCodeStatus = PerformStatus.None)
        } else {
            copy(checkPinCodeStatus = PerformStatus.Done(e))
        }

    private fun ModelState.cleanPinCodeResult() = copy(checkPinCodeStatus = PerformStatus.None)

    private fun ModelState.requestPerforming() = copy(queryPinCodeStatus = PerformStatus.Performing)

    private fun ModelState.requestSuccess() = copy(queryPinCodeStatus = PerformStatus.Done())

    private fun ModelState.requestFailed(e: Throwable) =
        if (e is CancellationException) {
            copy(queryPinCodeStatus = PerformStatus.None)
        } else {
            copy(queryPinCodeStatus = PerformStatus.Done(e))
        }

    private fun ModelState.cleanRequestResult() = copy(queryPinCodeStatus = PerformStatus.None)

}