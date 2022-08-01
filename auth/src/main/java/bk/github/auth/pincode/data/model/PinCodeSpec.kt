package bk.github.auth.pincode.data.model

data class PinCodeSpec(
    val id: String,
    val length: Int = 0,
    val numberOfAttempts: Int = 0,
    val expirationTime: Long = 0,
    val requestTimeout: Long = 0,
    val value: String? = null,
)
