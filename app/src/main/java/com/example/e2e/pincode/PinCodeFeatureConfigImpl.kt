package com.example.e2e.pincode

import android.app.Application
import bk.github.auth.R
import bk.github.auth.pincode.PinCodeFeatureConfig
import bk.github.auth.pincode.data.PinCodeManagerImpl
import bk.github.auth.pincode.WrongPinCodeException

class PinCodeFeatureConfigImpl(private val application: Application) : PinCodeFeatureConfig {

    override val formatter = PinCodeFormatterImpl(application)
    override val errorMapper = ::mapPinCodeError

    private fun mapPinCodeError(error: PinCodeManagerImpl.Error): WrongPinCodeException {
        return WrongPinCodeException(
            when (error) {
                PinCodeManagerImpl.Error.WrongPinCode -> application.getString(R.string.auth_code_wrong_pin_code)
            }
        )
    }

}