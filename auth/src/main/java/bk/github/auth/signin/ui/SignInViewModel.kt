package bk.github.auth.signin.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bk.github.auth.signin.data.LoginNotFoundException
import bk.github.auth.signin.data.SignInManager
import bk.github.auth.signin.data.SignInValidator
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@Suppress("unused", "NOTHING_TO_INLINE")
class SignInViewModel(
    private val manager: SignInManager,
    private val nicknameValidator: SignInValidator,
    private val passwordValidator: SignInValidator
) : ViewModel() {

    val availableLogins = manager.observeAvailableLogins()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = arrayOf()
        )

    val nickname = MutableStateFlow<String?>(null)
    val password = MutableStateFlow<String?>(null)

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private var signInJob: Job? = null

    fun nicknameChanged(value: String, complete: Boolean = false) {
        updateUiState {
            it.copy(nicknameError = nicknameValidator.validate(value, complete))
        }
    }

    fun passwordChanged(value: String, complete: Boolean = false) {
        updateUiState {
            it.copy(passwordError = passwordValidator.validate(value, complete))
        }
    }

    fun signIn(nickname: String, password: String) {
        signInJob?.cancel()
        signInJob = updateUiState {
            val nicknameError = nicknameValidator.validate(nickname, true)
            val passwordError = passwordValidator.validate(password, true)
            if (nicknameError != null || passwordError != null) {
                return@updateUiState UiState(
                    nicknameError = nicknameError, passwordError = passwordError
                )
            }

            _uiState.value = UiState(signInStatus = SignInStatus.Signing)
            manager.signIn(nickname, password).fold(
                onSuccess = { UiState(signInStatus = SignInStatus.SignedIn) },
                onFailure = {
                    if (it is LoginNotFoundException) {
                        UiState(
                            signInStatus = SignInStatus.NeedSignUp,
                            nicknameError = it.localizedMessage
                        )
                    } else {
                        UiState(signInFailure = it)
                    }
                }
            )
        }
    }

    fun signInErrorDone() {
        _uiState.value = _uiState.value.copy(
            signInStatus = SignInStatus.Unsigned, signInFailure = null
        )
    }

    private fun updateUiState(action: suspend (UiState) -> UiState) =
        viewModelScope.launch { _uiState.value = action(_uiState.value) }

    data class UiState(
        val signInStatus: SignInStatus = SignInStatus.Unsigned,
        val nicknameError: String? = null,
        val passwordError: String? = null,
        val signInFailure: Throwable? = null
    )

    enum class SignInStatus { Unsigned, Signing, SignedIn, NeedSignUp }

}


