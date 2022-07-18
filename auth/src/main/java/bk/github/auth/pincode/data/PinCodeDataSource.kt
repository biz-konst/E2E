package bk.github.auth.pincode.data

import bk.github.auth.pincode.data.model.PinCodeSecret
import bk.github.auth.pincode.data.model.PinCodeState

interface PinCodeDataSource {
    suspend fun requestPinCode(id: String?): Result<PinCodeState>
    suspend fun acceptPinCode(pinCode: PinCodeSecret): Result<*>
}
