package com.example.e2e.signin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import bk.github.auth.signin.ui.SignInFragment
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

    override fun getViewModelFactory() = SignInViewModelFactory(requireActivity().application)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
//            if (insets.isVisible(WindowInsetsCompat.Type.ime())) {
//                val r = Rect()
//                if (signInBinding.signInContainer.getGlobalVisibleRect(r)) {
//                }
//                val i = insets.getInsets(WindowInsetsCompat.Type.ime())
//                val b = v.context.windowManager!!.windowSize.y - i.bottom
//                binding.root.scrollBy(0, (r.bottom - b).coerceAtLeast(0))
//                Log.d(
//                    "signin",
//                    "setOnApplyWindowInsetsListener insets=$i, r=$r, b=$b, scroll=${binding.root.scrollY}"
//                )
//            } else {
//                binding.root.scrollY = 0
//                Log.d("signin", "setOnApplyWindowInsetsListener insets=$insets")
//            }
//            insets
//        }
    }

}