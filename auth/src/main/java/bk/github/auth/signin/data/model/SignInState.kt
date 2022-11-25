package bk.github.auth.signin.data.model

data class SignInState(
    val server: String?,
    val signedIn: Boolean,
    val signInUnlockTime: Long,
    val availableNicknames: Set<String>
)
