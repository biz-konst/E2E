@file:Suppress("unused")

package bk.github.auth.signin.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bk.github.auth.signin.SignInFeatureConfig
import bk.github.auth.signin.data.SignInManager
import bk.github.auth.signin.data.model.SignInData
import bk.github.auth.utils.SingletonJob
import bk.github.auth.utils.launchLatest
import bk.github.auth.utils.runSafely
import bk.github.auth.utils.startCountdownTimer
import bk.github.tools.switchMapLatest
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class SignInViewModel(
    val config: SignInFeatureConfig = SignInFeatureConfig(),
    val manager: SignInManager,
    val serverValidator: SignInInputValidator = SignInInputValidator.Concrete(),
    val nicknameValidator: SignInInputValidator = SignInInputValidator.Concrete(),
    val passwordValidator: SignInInputValidator = SignInInputValidator.Concrete(),
) : ViewModel() {

    companion object {
        const val TAG = "Auth.SignInViewModel"
    }

    private val _uiState = MutableStateFlow(UiState(SignInStatus.Input))
    val uiState = _uiState.asStateFlow()

    private val bkJob = SupervisorJob(viewModelScope.coroutineContext[Job])
    private val bkScope = viewModelScope + Dispatchers.Default + bkJob

    private val servers = manager.observeServerList()
    private val server = MutableStateFlow<String?>(null)
    private val signInState = server.switchMapLatest { manager.observeSignInState(it) }

    private val serverValidateJob = SingletonJob()
    private val nicknameValidateJob = SingletonJob()
    private val passwordValidateJob = SingletonJob()
    private val signInJob = SingletonJob()

    init {
        viewModelScope.launch {
            SharingStarted.WhileSubscribed(config.stopUiStateSharingMs)
                .command(_uiState.subscriptionCount).collectLatest {
                    if (it == SharingCommand.STOP) bkJob.cancelChildren()
                    else if (it == SharingCommand.START) internalStart()
                }
        }
    }

    fun signIn(data: SignInData) {
        var failure: Throwable? = null
        _uiState.update { it.copy(status = SignInStatus.Signing) }
        bkScope.launchLatest(signInJob) {
            validateSignInData(data)
            if (_uiState.value.noInputError) {
                manager.runSafely { signIn(data) }.let { r ->
                    if (manager.signUpNeeded(r)) {
                        signUp(data)
                        return@launchLatest
                    }
                    failure = r.exceptionOrNull().takeIf { it !is CancellationException }
                }
            }
            _uiState.update {
                it.copy(status = SignInStatus.Input, signInFailure = failure)
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun signUp(data: SignInData) {
        _uiState.update { it.copy(status = SignInStatus.Unsigned) }
    }

    fun clearSignInFailure(failure: Throwable) {
        _uiState.update {
            if (it.signInFailure == failure) it.copy(signInFailure = null) else it
        }
    }

    fun serverChanged(text: String) {
        bkScope.launchLatest(serverValidateJob) {
            val err = serverValidator(text) ?: null.also { server.value = text }
            _uiState.update { it.copy(serverError = err) }
        }
    }

    fun nicknameChanged(text: String) {
        bkScope.launchLatest(nicknameValidateJob) {
            _uiState.update { it.copy(nicknameError = nicknameValidator(text)) }
        }
    }

    fun passwordChanged(text: String) {
        bkScope.launchLatest(passwordValidateJob) {
            _uiState.update { it.copy(passwordError = passwordValidator(text)) }
        }
    }

    suspend fun validateSignInData(data: SignInData) {
        serverChanged(data.server)
        nicknameChanged(data.nickname)
        passwordChanged(data.password)
        serverValidateJob.job?.join()
        nicknameValidateJob.job?.join()
        passwordValidateJob.job?.join()
    }

    private fun internalStart() {
        signInState
            .combine(servers) { state, servers ->
                _uiState.update {
                    it.copy(
                        status = if (state.signedIn) SignInStatus.SignedIn else it.status,
                        serverList = servers,
                        server = state.server,
                        availableNicknames = state.availableNicknames.toList()
                    )
                }
            }.launchIn(bkScope)
        signInState
            .map { it.signInUnlockTime }.distinctUntilChanged()
            .switchMapLatest { startCountdownTimer(it) }
            .onEach { t -> _uiState.update { it.copy(signInTimeout = t) } }
            .launchIn(bkScope)
    }

    data class UiState(
        val status: SignInStatus,
        val serverList: List<String> = emptyList(),
        val server: String? = null,
        val availableNicknames: List<String> = emptyList(),
        val serverError: String? = null,
        val nicknameError: String? = null,
        val passwordError: String? = null,
        val signInFailure: Throwable? = null,
        val signInTimeout: Long = 0,
    ) {
        inline val noInputError
            get() = serverError == null && nicknameError == null && passwordError == null
    }

    enum class SignInStatus { Input, Signing, SignedIn, Unsigned }

}