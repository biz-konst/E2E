package bk.github.auth.pincode

import bk.github.auth.animation.AuthAnimation
import bk.github.auth.pincode.ui.PinCodeFormatter
import bk.github.auth.pincode.ui.PinCodeFragment

interface PinCodeFeatureConfig {
    val errorCleaningDelayMs: Long get() = ERROR_CLEANING_DELAY_MS
    val pinCodeUnlockDelayMs: Long get() = PIN_CODE_UNLOCK_DELAY_MS
    val stopUiStateFlowTimeoutMs: Long get() = STOP_UI_STATE_FLOW_TIMEOUT_MS
    val formatter: PinCodeFormatter
    val animation: PinCodeFragment.Animation get() = AuthAnimation()

    companion object {
        private const val ERROR_CLEANING_DELAY_MS = 12_000L
        private const val PIN_CODE_UNLOCK_DELAY_MS = 500L
        private const val STOP_UI_STATE_FLOW_TIMEOUT_MS = 5_000L
    }
}