package bk.github.auth.pincode.data.model

data class PinCodeState(
    val id: String,
    val length: Int = 0,
    val numberOfAttempts: Int = 0,
    val attemptsSpent: Int = 0,
    val expirationTime: Long = 0,
    val queryUnlockTime: Long = 0,
    val value: String? = null,
) {
    companion object {
        val EMPTY = PinCodeState("")
    }
}
