package com.example.e2e.pincode

import android.app.Application
import bk.github.auth.pincode.WrongPinCodeException
import bk.github.auth.pincode.data.PinCodeDataSource
import bk.github.auth.pincode.data.model.PinCodeSpec
import bk.github.auth.pincode.data.model.PinCodeValue
import com.example.e2e.R
import kotlinx.coroutines.delay

class PinCodeDataSourceTest(private val application: Application) : PinCodeDataSource {

    private val secret = PinCodeValue(id = "1", value = "1111")

    val spec
        get() = PinCodeSpec(
            id = secret.id,
            length = 4,
            numberOfAttempts = 3,
            expirationTime = Long.MAX_VALUE,//System.currentTimeMillis() + 25_000L,
            queryUnlockTime = 5_000L,
        )

    private var requestCount = 0

    override suspend fun requestPinCode(id: String?): Result<PinCodeSpec> {
        delay(2000)
        return Result.success(spec)
    }

    override suspend fun acceptPinCode(pinCode: PinCodeValue): Result<*> {
        delay(1000)
        val res = if (pinCode == secret) {
            Result.success(Unit)
        } else {
            Result.failure<Any>(WrongPinCodeException(application.getString(R.string.auth_code_invalid_pin_code)))
        }
        return res
    }

}
