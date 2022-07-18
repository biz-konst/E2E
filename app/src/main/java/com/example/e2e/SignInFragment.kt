package com.example.e2e

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.Toast
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import bk.github.auth.signin.data.SignInManagerImpl
import bk.github.auth.signin.data.SignInValidatorImpl
import bk.github.auth.signin.ui.SignInFragment
import bk.github.auth.signin.ui.SignInViewModel
import bk.github.insets.Dimensions
import bk.github.insets.applyInsets
import bk.github.tools.windowManager
import bk.github.tools.windowSize
import com.example.e2e.databinding.FragmentSignInBinding

class SignInFragment : SignInFragment() {

    private lateinit var binding: FragmentSignInBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            if (insets.isVisible(WindowInsetsCompat.Type.ime())) {
                val r = Rect()
                if (signInBinding.signInContainer.getGlobalVisibleRect(r)) {
                }
                val i = insets.getInsets(WindowInsetsCompat.Type.ime())
                val b = v.context.windowManager!!.windowSize.y - i.bottom
                binding.root.scrollBy(0, (r.bottom - b).coerceAtLeast(0))
                Log.d(
                    "signin",
                    "setOnApplyWindowInsetsListener insets=$i, r=$r, b=$b, scroll=${binding.root.scrollY}"
                )
            } else {
                binding.root.scrollY = 0
                Log.d("signin", "setOnApplyWindowInsetsListener insets=$insets")
            }
            insets
        }
    }

    override fun getViewModelFactory() = object : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SignInViewModel(
                SignInManagerImpl(SignInDataSourceTest()),
                SignInValidatorImpl(),
                SignInValidatorImpl()
                    .addCondition(".{2,}", getString(R.string.auth_sign_password_too_short)),
            ) as T
    }

    override fun onStateChanged(state: SignInViewModel.UiState) {
        super.onStateChanged(state)
        binding.progressBar.isVisible = isLoading(state)
    }

    override fun signedIn(nickname: String, password: String) {
        findNavController().navigate(R.id.action_signInFragment_to_codeFragment)
    }

    override fun onSignInError(error: Throwable): Boolean {
        Toast.makeText(context, error.localizedMessage, Toast.LENGTH_SHORT).show()
        return super.onSignInError(error)
    }

    private fun isLoading(state: SignInViewModel.UiState): Boolean {
        return state.signInStatus == SignInViewModel.SignInStatus.Signing
    }

}