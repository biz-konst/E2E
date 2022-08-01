package com.example.e2e.pincode

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import bk.github.auth.pincode.data.PinCodeManagerImpl
import bk.github.auth.pincode.data.model.asState
import bk.github.auth.pincode.ui.PinCodeViewModel

class PinCodeViewModelFactory(private val application: Application) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val config = PinCodeFeatureConfigImpl(application)
        val source = PinCodeDataSourceTest(application)
        val manager = PinCodeManagerImpl(source, config::mapPinCodeError, source.spec.asState())
        return PinCodeViewModel(manager, config) as T
    }
}