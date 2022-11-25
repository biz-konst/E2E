package com.example.e2e.pincode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import bk.github.auth.pincode.ui.PinCodeFrag
import com.example.e2e.databinding.FragmentPinCodeBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class PinCodeFragment : PinCodeFrag() {

    //    private lateinit var binding: FragmentPinCodeBinding
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        // Inflate the layout for this fragment
//        binding = FragmentPinCodeBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun getViewModelFactory() = PinCodeViewModelFactory(requireActivity().application)
//
//    override fun pinCodeAccept() = Unit
//
//    override fun onPinCodeError(error: Throwable): Boolean {
//        if (error is WrongPinCodeException) {
//            return super.onPinCodeError(error)
//        }
//
//        Snackbar.make(requireView().rootView, error.requireMessage, Snackbar.LENGTH_SHORT).show()
////        Toast.makeText(context, error.requireMessage, Toast.LENGTH_SHORT).show()
//        return true
//    }
//
//    override fun onRequestError(error: Throwable): Boolean {
//        Snackbar.make(requireView().rootView, error.requireMessage, Snackbar.LENGTH_SHORT).show()
////        Toast.makeText(context, error.requireMessage, Toast.LENGTH_SHORT).show()
//        return super.onRequestError(error)
//    }
//
////    private class ShakeAnimator(view: View) {
////        private val animator =
////            SpringAnimation(view, DynamicAnimation.TRANSLATION_X, 0f)
////
////        init {
////            animator.spring.apply {
////                dampingRatio = SpringForce.DAMPING_RATIO_HIGH_BOUNCY
////                stiffness = SpringForce.STIFFNESS_MEDIUM
////            }
////        }
////
////        fun start(startValue: Float) {
////            animator.setStartValue(startValue)
////            //animator.start()
////        }
////
////        fun stop() {
////            with(animator) { if (canSkipToEnd()) skipToEnd() else cancel() }
////        }
////    }

    private lateinit var binding: FragmentPinCodeBinding

    override fun createViewModel(): PinCodeVMStub = PinCodeVMStub()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentPinCodeBinding.inflate(inflater, container, false)
        return binding.root
    }

}