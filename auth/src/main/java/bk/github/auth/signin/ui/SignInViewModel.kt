package bk.github.auth.signin.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bk.github.auth.signin.SignInFeatureConfig
import bk.github.auth.signin.data.SignInManager
import bk.github.auth.signin.data.model.SignInData
import bk.github.auth.utils.runSafely
import bk.github.auth.utils.startCountdownTimer
import bk.github.tools.singletonLauncher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.plus

@Suppress("unused", "MemberVisibilityCanBePrivate", "CanBeParameter")
class SignInViewModel(
    private val manager: SignInManager,
    val config: SignInFeatureConfig,
    private val nicknameValidator: SignInInputValidator,
    private val passwordValidator: SignInInputValidator
) : ViewModel() {

    companion object {
        const val TAG = "Auth.SignInViewModel"
    }

    private val defaultDispatcherScope = viewModelScope + Dispatchers.Default

    private val servers = manager.observeServerList()
    private val server = MutableStateFlow<String?>(null)

    @ExperimentalCoroutinesApi
    private val signInState = server
        .flatMapLatest { manager.observeSignInState(it) }

    private val nicknameError = MutableStateFlow<String?>(null)
    private val passwordError = MutableStateFlow<String?>(null)
    private val eventState = MutableStateFlow(EventState.input)

    @ExperimentalCoroutinesApi
    val uiState =
        combine(
            servers,
            signInState,
            nicknameError,
            passwordError,
            eventState
        ) { servers, state, nicknameError, passwordError, eventState ->
            UiState(
                status = eventState.status,
                servers = servers,
                server = state.server,
                availableNicknames = state.availableNicknames,
                nicknameError = nicknameError,
                passwordError = passwordError,
                signInFailure = eventState.failure
            )
        }.stateIn(
            scope = defaultDispatcherScope,
            started = SharingStarted.WhileSubscribed(
                stopTimeoutMillis = config.stopUiStateFlowTimeoutMs
            ),
            initialValue = UiState.INITIAL
        )

    @ExperimentalCoroutinesApi
    val signInLockTimer = signInState
        .map { it.signInUnlockTime }.distinctUntilChanged()
        .flatMapLatest { startCountdownTimer(it) }
    //.flowOn(defaultDispatcherScope.coroutineContext)

    private val checkNicknameLauncher = defaultDispatcherScope.singletonLauncher()
    private val checkPasswordLauncher = defaultDispatcherScope.singletonLauncher()
    private val signInLauncher = defaultDispatcherScope.singletonLauncher()
    private val signInCancelLauncher = defaultDispatcherScope.singletonLauncher()

    fun selectServer(value: String?) {
        server.value = value
    }

    fun checkNickname(value: String) {
        checkNicknameLauncher.launchLatest {
            nicknameError.value = nicknameValidator(value)
        }
    }

    fun checkPassword(value: String) {
        checkPasswordLauncher.launchLatest {
            passwordError.value = passwordValidator(value)
        }
    }

    fun signIn(value: SignInData) {
        signInLauncher.launchLatest {
            eventState.value = EventState.signingIn
            eventState.value = doSignIn(value).fold(
                onSuccess = { EventState.signedIn },
                onFailure = EventState::failure
            )
        }
        signInCancelLauncher.launch {
            eventState.subscriptionCount
                .filter { it == 0 }
                .collect { signInLauncher.cancel() }
        }
    }

    fun signIn(server: String?, nickname: String, password: String) {
        signIn(SignInData(server, nickname, password))
    }

    fun failureDone() {
        eventState.apply { compareAndSet(value, value.copy(failure = null)) }
    }

    private suspend fun doSignIn(value: SignInData): Result<*> {
        return runSafely { manager.signIn(value) }
    }

    enum class UiStatus { Initial, Input, SigningIn, SignedIn }

    data class UiState(
        val status: UiStatus = UiStatus.Initial,
        val servers: List<String> = emptyList(),
        val server: String? = null,
        val availableNicknames: List<String> = emptyList(),
        val nicknameError: String? = null,
        val passwordError: String? = null,
        val signInFailure: Throwable? = null,
    ) {
        companion object {
            val INITIAL = UiState()
        }
    }

    private data class EventState(
        val status: UiStatus,
        val failure: Throwable? = null
    ) {
        companion object {
            val input = EventState(status = UiStatus.Input)
            val signingIn = EventState(status = UiStatus.SigningIn)
            val signedIn = EventState(status = UiStatus.SignedIn)
            fun failure(e: Throwable) = when (e) {
                is CancellationException -> input
                else -> EventState(status = UiStatus.Input, failure = e)
            }
        }
    }

}

