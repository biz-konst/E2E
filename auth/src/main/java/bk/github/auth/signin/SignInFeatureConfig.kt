package bk.github.auth.signin

private const val STOP_UI_STATE_SHARING_MS = 5_000L

class SignInFeatureConfig(
    val stopUiStateSharingMs: Long = STOP_UI_STATE_SHARING_MS,
    val serverPresent: Boolean = false,
    val useSignUpButton: Boolean = false,
)