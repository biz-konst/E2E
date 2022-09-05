package com.example.e2e.signin

import android.app.Application
import bk.github.auth.signin.SignInFeatureConfig
import bk.github.auth.signin.ui.SignInInputValidator
import bk.github.auth.signin.ui.SignInInputValidatorImpl

class SignInFeatureConfigImpl(private val application: Application) : SignInFeatureConfig {
    val nicknameValidator: SignInInputValidator = SignInInputValidatorImpl()
    val passwordValidator: SignInInputValidator = SignInInputValidatorImpl()
}