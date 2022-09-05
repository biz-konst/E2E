package bk.github.auth.pincode.data

import bk.github.auth.pincode.data.model.PinCodeState
import bk.github.auth.pincode.data.model.PinCodeValue
import kotlinx.coroutines.flow.Flow

interface PinCodeManager {
    fun observePinCodeState(): Flow<PinCodeState>
    suspend fun requestPinCode(id: String?): Result<PinCodeState>
    suspend fun acceptPinCode(secret: PinCodeValue): Result<*>
}