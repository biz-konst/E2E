package com.example.e2e.signin

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import bk.github.auth.pincode.data.PinCodeManagerImpl
import bk.github.auth.pincode.data.model.asState
import bk.github.auth.pincode.ui.PinCodeViewModel
import bk.github.auth.signin.data.SignInManager
import bk.github.auth.signin.data.SignInManagerImpl
import bk.github.auth.signin.ui.SignInViewModel
import com.example.e2e.pincode.PinCodeDataSourceTest

class SignInViewModelFactory(private val application: Application) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val config = SignInFeatureConfigImpl(application)
        val source = SignInDataSourceTest()
        val manager = SignInManagerImpl(source)
        return SignInViewModel(manager, config, config.nicknameValidator, config.passwordValidator) as T
    }
}