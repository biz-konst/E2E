package com.example.e2e.pincode

import android.app.Application
import bk.github.auth.pincode.data.PinCodeDataSource
import bk.github.auth.pincode.WrongPinCodeException
import bk.github.auth.pincode.data.model.PinCodeSecret
import bk.github.auth.pincode.data.model.PinCodeState
import com.example.e2e.R
import kotlinx.coroutines.delay

class PinCodeDataSourceTest(private val application: Application) : PinCodeDataSource {

    private val secret = PinCodeSecret(id = "1", value = "1111")

    val state
        get() = PinCodeState(
            id = secret.id,
            length = 4,
            numberOfAttempts = 3,
            expirationTime = Long.MAX_VALUE,//System.currentTimeMillis() + 25_000L,
            requestTimeout = 5_000L,
            lastRequestTime = 0,
        )

    private var requestCount = 0

    override suspend fun requestPinCode(id: String?): Result<PinCodeState> {
        delay(2000)
        return Result.success(state)
    }

    override suspend fun acceptPinCode(pinCode: PinCodeSecret): Result<*> {
        delay(1000)
        val res = if (pinCode == secret) {
            Result.success(Unit)
        } else {
            Result.failure<Any>(WrongPinCodeException(application.getString(R.string.auth_code_invalid_pin_code)))
        }
        return res
    }

}
