package bk.github.auth.signin.ui

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import bk.github.auth.R
import bk.github.auth.databinding.SignInFragmentBinding
import bk.github.auth.signin.data.model.SignInData
import bk.github.auth.utils.TimeoutShowHelper
import bk.github.tools.doOnClick
import bk.github.tools.launchWhenStarted
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.plus

@Suppress("MemberVisibilityCanBePrivate", "unused")
abstract class SignInFragment : Fragment() {

    companion object {
        const val TAG = "Auth.SignInFragment"

        private val ATTRS = intArrayOf(R.attr.authSignInDropDownItemLayout)
    }

    open val signInViewModel: SignInViewModel by viewModels {
        viewModelFactory {
            initializer { createViewModel() }
        }
    }

    lateinit var signInBinding: SignInFragmentBinding

    inline val config get() = signInViewModel.config

    private var dropdownItemId = android.R.layout.simple_spinner_dropdown_item

    private lateinit var serverAdapter: SpinnerAdapter<String>
    private lateinit var nicknameAdapter: SpinnerAdapter<String>

    private var lastSignInFailure: Throwable? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        signInBinding = SignInFragmentBinding.bind(view.findViewById(R.id.signInContainer))
        initAttrs(signInBinding.signInContainer.context)
        initViews()
        bindViewModel()
        startupCheck()
    }

    abstract fun createViewModel(): SignInViewModel

    open fun navigateToSigned() {}

    open fun navigateToSignUp() {}

    open fun onInput(state: SignInViewModel.UiState) {
        showSignInProgress(false)
        setInputEnabled(true)
        setSelectedServer(state.server)
        setInputError(state.serverError, state.nicknameError, state.passwordError)
        val actionEnabled = state.noInputError
        setSignInEnabled(actionEnabled && state.signInTimeout == 0L)
        setSignUpEnabled(actionEnabled)
        doSignInError(state.signInFailure)
    }

    open fun onUnsigned(state: SignInViewModel.UiState) {
        showSignInProgress(false)
        navigateToSignUp()
    }

    open fun onSignedIn(state: SignInViewModel.UiState) {
        showSignInProgress(false)
        navigateToSigned()
    }

    open fun onSigning(state: SignInViewModel.UiState) {
        setInputEnabled(false)
        setSignInEnabled(false)
        setSignUpEnabled(false)
        showSignInProgress(true)
    }

    open fun onSignInError(failure: Throwable) {
        showSignInError(failure)
        signInViewModel.clearSignInFailure(failure)
    }

    open fun showSignInProgress(show: Boolean) {
        signInBinding.signInButtonLayout.isProgressShown = show
    }

    open fun showSignInError(failure: Throwable) {}

    open fun showSignInTimeout(timeout: Long) {
        setTimeoutOnSignInButton(if (timeout == 0L) null else formatSignInTimeout(timeout))
    }

    open fun formatSignInTimeout(timeout: Long): String? = null

    open fun startupCheck() {
        launchWhenStarted { signInViewModel.validateSignInData(obtainSignInData()) }
    }

    fun setInputEnabled(enabled: Boolean) {
        with(signInBinding) {
            serverInputLayout.enable(enabled)
            nicknameInputLayout.enable(enabled)
            passwordInputLayout.enable(enabled)
        }
    }

    fun setInputError(
        serverError: String? = null,
        nicknameError: String? = null,
        passwordError: String? = null
    ) {
        with(signInBinding) {
            serverInputLayout.error = serverError
            nicknameInputLayout.error = nicknameError
            passwordInputLayout.error = passwordError
        }
    }

    fun setSignInEnabled(enabled: Boolean) {
        signInBinding.signInButton.isEnabled = enabled
    }

    fun setSignUpEnabled(enabled: Boolean) {
        signInBinding.signUpButton.isEnabled = enabled
    }

    fun serverChanged(text: String) {
        signInViewModel.serverChanged(text)
    }

    fun nicknameChanged(text: String) {
        signInViewModel.nicknameChanged(text)
    }

    fun passwordChanged(text: String) {
        signInViewModel.passwordChanged(text)
    }

    fun signIn() = signInViewModel.signIn(obtainSignInData())

    fun signUp() = signInViewModel.signUp(obtainSignInData())

    private fun initAttrs(context: Context) {
        with(context.theme.obtainStyledAttributes(ATTRS)) {
            dropdownItemId = getResourceId(0, dropdownItemId)
            recycle()
        }
    }

    private fun initViews() {
        with(signInBinding) {
            with(config) {
                serverInputLayout.isVisible = serverPresent
                if (serverPresent) {
                    serverEdit.apply {
                        serverAdapter = createAdapter(isEditable(this), dropdownItemId)
                        setupEditText(this, serverAdapter, ::serverChanged)
                    }
                }
                nicknameEdit.apply {
                    nicknameAdapter = createAdapter(isEditable(this), dropdownItemId)
                    setupEditText(this, nicknameAdapter, ::nicknameChanged)
                }
                passwordEdit.apply {
                    onAfterTextChanged(::passwordChanged)
                }
                signInButton.doOnClick { signIn() }
                signUpButton.isVisible = useSignUpButton
                if (useSignUpButton) {
                    signUpButton.doOnClick { signUp() }
                }
            }
        }
    }

    private fun bindViewModel() {
        launchWhenStarted {
            signInViewModel.uiState.apply {
                if (config.serverPresent) {
                    map { it.serverList }.distinctUntilChanged()
                        .onEach { submitItems(serverAdapter, it, signInBinding.serverEdit) }
                        .launchIn(this@launchWhenStarted + Dispatchers.Default)
                }
                map { it.availableNicknames }.distinctUntilChanged()
                    .onEach { submitItems(nicknameAdapter, it, signInBinding.nicknameEdit) }
                    .launchIn(this@launchWhenStarted + Dispatchers.Default)
                collect { uiStateChanged(it) }
            }
        }
    }

    private fun createAdapter(editable: Boolean, resource: Int) =
        SpinnerAdapter<String>(requireContext(), resource, excludeFilter = !editable)

    private fun setupEditText(
        view: EditText,
        adapter: SpinnerAdapter<String>,
        onChangedListener: (String) -> Unit
    ) {
        if (view is AutoCompleteTextView) {
            view.setAdapter(adapter)
            if (adapter.excludeFilter) {
                view.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
                    adapter.filter.filter(view.text)
                    onChangedListener(view.text?.toString() ?: "")
                }
                return
            }
        }
        view.onAfterTextChanged(onChangedListener)
    }

    private fun uiStateChanged(state: SignInViewModel.UiState) {
        when (state.status) {
            SignInViewModel.SignInStatus.Input -> onInput(state)
            SignInViewModel.SignInStatus.Signing -> onSigning(state)
            SignInViewModel.SignInStatus.SignedIn -> onSignedIn(state)
            SignInViewModel.SignInStatus.Unsigned -> onUnsigned(state)
        }
        showSignInTimeout(state.signInTimeout)
    }

    private fun setSelectedServer(server: String?) {
        with(signInBinding.serverEdit) {
            if (config.serverPresent && text.toString() != server) {
                setText(server)
                (adapter as Filterable).filter.filter(server)
            }
        }
    }

    private fun doSignInError(failure: Throwable?) {
        if (lastSignInFailure != failure) {
            lastSignInFailure = failure
            if (failure != null) onSignInError(failure)
        }
    }

    private fun obtainSignInData() = with(signInBinding) {
        SignInData(
            server = serverEdit.text?.toString() ?: "",
            nickname = nicknameEdit.text.toString(),
            password = passwordEdit.text?.toString() ?: ""
        )
    }

    private fun isEditable(view: EditText): Boolean =
        view.editableText != null && view.onCheckIsTextEditor()

    private fun setTimeoutOnSignInButton(text: String?) {
        TimeoutShowHelper.showTimeout(signInBinding.signInButton, text ?: return)
    }

    private suspend fun submitItems(
        adapter: SpinnerAdapter<String>,
        items: List<String>,
        view: EditText
    ) {
        adapter.submitItems(items) {
            if (adapter.excludeFilter) {
                adapter.filter.filter(view.text) {
                    if (it == -1) view.text = null
                }
            } else {
                adapter.filter.filter(null)
            }
        }
    }

    private fun TextInputLayout.enable(enabled: Boolean) {
        if (isEnabled != enabled) isEnabled = enabled
    }

    private fun EditText.onAfterTextChanged(action: (String) -> Unit) =
        addTextChangedListener(MyWatcher(action))

    private class MyWatcher(val action: (String) -> Unit) : TextWatcher {
        private var changed = false

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

        override fun afterTextChanged(s: Editable?) {
            if (!(changed || s.isNullOrEmpty())) changed = true
            if (changed) action(s.toString())
        }

    }

}

