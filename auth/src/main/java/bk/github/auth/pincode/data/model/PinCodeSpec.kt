package bk.github.auth.pincode.data.model

data class PinCodeSpec(
    val id: String,
    val length: Int = 0,
    val numberOfAttempts: Int = 0,
    val expirationTime: Long = 0,
    val queryUnlockTime: Long = 0,
    val value: String? = null,
)

fun PinCodeSpec.asState(attemptsSpent: Int = 0) = PinCodeState(
    id = id,
    length = length,
    numberOfAttempts = numberOfAttempts,
    expirationTime = expirationTime,
    queryUnlockTime = queryUnlockTime,
    value = value,
    attemptsSpent = attemptsSpent,
)