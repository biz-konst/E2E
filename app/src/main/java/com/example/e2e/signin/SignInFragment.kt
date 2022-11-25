package com.example.e2e.signin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import bk.github.auth.signin.SignInFeatureConfig
import bk.github.auth.signin.data.SignInManager
import bk.github.auth.signin.ui.SignInFragment
import bk.github.auth.signin.ui.SignInInputValidator
import bk.github.auth.signin.ui.SignInViewModel
import bk.github.tools.longToast
import bk.github.tools.requireMessage
import com.example.e2e.R
import com.example.e2e.TimeoutFormatter
import com.example.e2e.databinding.FragmentSignInBinding

//class SignInFragment : SignInFragment() {
//
//    override val signInViewModel: SignInViewModel by viewModels {
//        SignInViewModelFactory(requireActivity().application)
//    }
//
//    private lateinit var binding: FragmentSignInBinding
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        // Inflate the layout for this fragment
//        binding = FragmentSignInBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        //        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
////            if (insets.isVisible(WindowInsetsCompat.Type.ime())) {
////                val r = Rect()
////                if (signInBinding.signInContainer.getGlobalVisibleRect(r)) {
////                }
////                val i = insets.getInsets(WindowInsetsCompat.Type.ime())
////                val b = v.context.windowManager!!.windowSize.y - i.bottom
////                binding.root.scrollBy(0, (r.bottom - b).coerceAtLeast(0))
////                Log.d(
////                    "signin",
////                    "setOnApplyWindowInsetsListener insets=$i, r=$r, b=$b, scroll=${binding.root.scrollY}"
////                )
////            } else {
////                binding.root.scrollY = 0
////                Log.d("signin", "setOnApplyWindowInsetsListener insets=$insets")
////            }
////            insets
////        }
//    }
//
//    override fun onSignInError(failure: Throwable) {
//        longToast(failure.requireMessage)
//        super.onSignInError(failure)
//    }
//
//}

class SignInFragment : SignInFragment() {

    private lateinit var binding: FragmentSignInBinding

    override fun createViewModel(): SignInViewModel =
        SignInViewModel(
            config = SignInFeatureConfig(serverPresent = true),
            manager = SignInManager.Default(SignInDataSourceTest()),
            serverValidator = SignInInputValidator.Concrete(),
            nicknameValidator = SignInInputValidator.Concrete(),
            passwordValidator = SignInInputValidator.Concrete(listOf(".+" to "must be at least 1 char")),
        )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun formatSignInTimeout(timeout: Long) =
        " (${TimeoutFormatter.format(timeout, requireContext())})"

    override fun showSignInError(failure: Throwable) {
        longToast(failure.requireMessage)
    }

}