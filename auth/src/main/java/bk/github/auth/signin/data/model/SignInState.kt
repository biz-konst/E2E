package bk.github.auth.signin.data.model

data class SignInState(
    val server: String?,
    val availableNicknames: List<String>,
    val signInUnlockTime: Long
)
