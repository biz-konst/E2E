package bk.github.auth.pincode.data

import bk.github.auth.pincode.data.model.PinCodeSpec
import bk.github.auth.pincode.data.model.PinCodeValue

interface PinCodeDataSource {
    suspend fun requestPinCode(id: String?): Result<PinCodeSpec>
    suspend fun acceptPinCode(pinCode: PinCodeValue): Result<*>
}
