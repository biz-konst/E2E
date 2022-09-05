package bk.github.auth.pincode.data

import bk.github.auth.pincode.WrongPinCodeException
import bk.github.auth.pincode.data.PinCodeManagerImpl.Error.WrongPinCode
import bk.github.auth.pincode.data.model.PinCodeState
import bk.github.auth.pincode.data.model.PinCodeValue
import bk.github.auth.pincode.data.model.asState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Suppress("MemberVisibilityCanBePrivate")
open class PinCodeManagerImpl(
    protected val source: PinCodeDataSource,
    protected val errorMapper: (error: Error) -> Throwable,
    initialState: PinCodeState = PinCodeState.EMPTY
) : PinCodeManager {

    enum class Error { WrongPinCode }

    protected var attemptNumber: Int = 0

    protected val pinCodeState = MutableStateFlow(initialState)

    override fun observePinCodeState(): Flow<PinCodeState> = pinCodeState.asStateFlow()

    override suspend fun requestPinCode(id: String?): Result<PinCodeState> {
        return source.requestPinCode(id)
            .map { spec ->
                spec.asState()
            }.onSuccess { s ->
                attemptNumber = s.attemptsSpent
                pinCodeState.update { s }
            }
    }

    override suspend fun acceptPinCode(secret: PinCodeValue): Result<*> {
        val encoded = encodePin(secret.value)
        val result = with(pinCodeState.value) {
            if (value != null && value != encoded) {
                Result.failure<Any>(errorMapper(WrongPinCode))
            } else {
                source.acceptPinCode(PinCodeValue(id, encoded))
            }
        }
        adjustAttemptNumber(result)
        return result
    }

    open fun encodePin(value: String): String = value

    private fun adjustAttemptNumber(resultOfAccept: Result<*>) {
        if (resultOfAccept.exceptionOrNull() is WrongPinCodeException) {
            attemptNumber++
            pinCodeState.update { it.copy(attemptsSpent = attemptNumber) }
        }
    }

}

