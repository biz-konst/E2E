package bk.github.auth.pincode.data

import bk.github.auth.pincode.PinCodeFeatureConfig
import bk.github.auth.pincode.data.PinCodeManagerImpl.Error.WrongPinCode
import bk.github.auth.pincode.data.model.PinCodeSecret
import bk.github.auth.pincode.data.model.PinCodeState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Suppress("MemberVisibilityCanBePrivate")
open class PinCodeManagerImpl(
    protected val source: PinCodeDataSource,
    protected val config: PinCodeFeatureConfig,
    initialState: PinCodeState = PinCodeState.EMPTY
) : PinCodeManager {

    enum class Error { WrongPinCode }

    protected val pinCodeState = MutableStateFlow(initialState)

    override fun observePinCodeState(): Flow<PinCodeState> = pinCodeState.asStateFlow()

    override suspend fun requestPinCode(id: String?): Result<PinCodeState> {
        return source.requestPinCode(id).map {
            it.copy(lastRequestTime = System.currentTimeMillis())
        }.onSuccess { pinCodeState.update { it } }
    }

    override suspend fun verifyPinCode(pinCode: String): String? = null

    override suspend fun acceptPinCode(secret: PinCodeSecret): Result<*> {
        val encoded = encodePin(secret.value)

        with(pinCodeState.value) {
            if (value != null && value != encoded) {
                return Result.failure<Any>(config.errorMapper(WrongPinCode))
            }

            return source.acceptPinCode(PinCodeSecret(id, encoded))
        }
    }

    open fun encodePin(value: String): String = value

}

