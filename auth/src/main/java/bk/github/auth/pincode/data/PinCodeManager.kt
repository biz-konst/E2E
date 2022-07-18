package bk.github.auth.pincode.data

import bk.github.auth.pincode.data.model.PinCodeSecret
import bk.github.auth.pincode.data.model.PinCodeState
import kotlinx.coroutines.flow.Flow

interface PinCodeManager {
    fun observePinCodeState(): Flow<PinCodeState>
    suspend fun requestPinCode(id: String?): Result<PinCodeState>
    suspend fun verifyPinCode(pinCode: String): String?
    suspend fun acceptPinCode(secret: PinCodeSecret): Result<*>
}