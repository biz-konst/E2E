package bk.github.auth.pincode.data.model

data class PinCodeState(
    val id: String,
    val length: Int = 0,
    val numberOfAttempts: Int = 0,
    val attemptsSpent: Int = 0,
    val expirationTime: Long = 0,
    val requestTimeout: Long = 0,
    val lastRequestTime: Long = 0,
    val value: String? = null,
) {
    companion object {
        val EMPTY = PinCodeState("")
    }
}

fun PinCodeSpec.asState(attemptsSpent: Int = 0, lastRequestTime: Long = 0) = PinCodeState(
    id = id,
    length = length,
    numberOfAttempts = numberOfAttempts,
    expirationTime = expirationTime,
    requestTimeout = requestTimeout,
    value = value,
    attemptsSpent = attemptsSpent,
    lastRequestTime = lastRequestTime,
)