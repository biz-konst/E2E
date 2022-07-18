package bk.github.auth.signin.ui

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import bk.github.auth.R
import bk.github.auth.databinding.SignInFragmentBinding
import bk.github.tools.doOnClick
import bk.github.tools.doOnLostFocus
import bk.github.tools.observeOnLifecycle

@Suppress("unused", "MemberVisibilityCanBePrivate")
abstract class SignInFragment : Fragment() {

    val viewModel: SignInViewModel by viewModels(
        ownerProducer = ::getViewModelOwner,
        factoryProducer = ::getViewModelFactory)

    lateinit var signInBinding: SignInFragmentBinding private set

    private val nicknameDropDownItemId = android.R.layout.simple_spinner_dropdown_item
    private val nicknameAdapter by lazy {
        ArrayAdapter<String>(requireContext(), nicknameDropDownItemId)
    }

    private inline val nickname: String get() = signInBinding.nicknameEdit.text.toString()
    private inline val password: String get() = signInBinding.passwordEdit.text.toString()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        signInBinding = SignInFragmentBinding.bind(view.findViewById(R.id.signInContainer))
        signInBinding.apply {
//            signInContainer.applyInsets {
//                applyImePadding { delta -> (delta * 2).coerceAtLeast(0) }
//            }
            nicknameEdit.apply {
                doOnTextChanged { s, _, _, _ -> viewModel.nicknameChanged(s.toString()) }
                doOnLostFocus { viewModel.nicknameChanged(nickname, complete = true) }
                setAdapter(nicknameAdapter)
            }
            passwordEdit.apply {
                doOnTextChanged { s, _, _, _ -> viewModel.passwordChanged(s.toString()) }
                doOnLostFocus { viewModel.passwordChanged(password, complete = true) }
                setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        viewModel.signIn(nickname, password)
                        return@setOnEditorActionListener true
                    }
                    false
                }
            }
            actionSignIn.doOnClick { viewModel.signIn(nickname, password) }
            with(viewModel) {
                observeOnLifecycle(nickname) {
                    if (!it.isNullOrEmpty()) nicknameEdit.setText(it)
                }
                observeOnLifecycle(password) {
                    if (!it.isNullOrEmpty()) passwordEdit.setText(it)
                }
                observeOnLifecycle(uiState) { onStateChanged(it) }
                observeOnLifecycle(availableLogins) { setNicknameAdapter(it) }
            }
        }
    }

    open fun getViewModelOwner(): ViewModelStoreOwner = this

    abstract fun getViewModelFactory(): ViewModelProvider.Factory

    open fun onStateChanged(state: SignInViewModel.UiState) {
        with(signInBinding) {
            @Suppress("NON_EXHAUSTIVE_WHEN_STATEMENT")
            when (state.signInStatus) {
                SignInViewModel.SignInStatus.SignedIn -> signedIn(nickname, password)
                SignInViewModel.SignInStatus.NeedSignUp -> signUp(nickname, password)
            }
            enableInput(inputIsEnable(state))
            enableSignInAction(signInIsEnable(state))
            nicknameInputLayout.error = state.nicknameError
            passwordInputLayout.error = state.passwordError
            showSignInError(state.signInFailure)
        }
    }

    abstract fun signedIn(nickname: String, password: String)

    open fun signUp(nickname: String, password: String) = Unit

    open fun onSignInError(error: Throwable) = true

    open fun inputIsEnable(state: SignInViewModel.UiState): Boolean =
        state.signInStatus == SignInViewModel.SignInStatus.Unsigned

    open fun signInIsEnable(state: SignInViewModel.UiState): Boolean =
        state.signInStatus == SignInViewModel.SignInStatus.Unsigned &&
                (state.nicknameError == null && state.passwordError == null)


    private fun setNicknameAdapter(logins: Array<String>) = nicknameAdapter.submit(logins)

    private fun showSignInError(error: Throwable?) {
        if (onSignInError(error ?: return)) viewModel.signInErrorDone()
    }

    private lateinit var adapter: ArrayAdapter<String>

    private fun <T> createDropDownAdapter(items: List<T>): ArrayAdapter<T> =
        ArrayAdapter(requireContext(), nicknameDropDownItemId, items)

    private fun enableInput(enable: Boolean) {
        with(signInBinding) {
            if (nicknameEdit.isEnabled != enable) {
                nicknameEdit.isEnabled = enable
            }
            if (passwordEdit.isEnabled != enable) {
                passwordEdit.isEnabled = enable
            }
        }
    }

    private fun enableSignInAction(enable: Boolean) {
        with(signInBinding.actionSignIn) {
            if (isEnabled != enable) isEnabled = enable
        }
    }

    private fun <T> ArrayAdapter<T>.submit(items: Array<T>) {
        setNotifyOnChange(false)
        clear()
        items.forEach { add(it) }
        filter.filter("")
    }

}

