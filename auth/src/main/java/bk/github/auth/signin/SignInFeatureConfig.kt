package bk.github.auth.signin

interface SignInFeatureConfig {
    val stopUiStateFlowTimeoutMs: Long get() = STOP_UI_STATE_FLOW_TIMEOUT_MS

    companion object {
        private const val STOP_UI_STATE_FLOW_TIMEOUT_MS = 5_000L
    }
}