package bk.github.auth.pincode

import bk.github.auth.R
import bk.github.auth.animation.AuthAnimation
import bk.github.auth.pincode.data.PinCodeManagerImpl
import bk.github.auth.pincode.ui.PinCodeFormatter
import bk.github.auth.pincode.ui.PinCodeFragment

interface PinCodeFeatureConfig {
    val formatter: PinCodeFormatter
    val animation: PinCodeFragment.Animation get() = AuthAnimation()
    val pinViewId: Int get() = R.layout.pin_code_pin_view
    val errorCleaningDelayMs: Long get() = 2_000L
    val pinCodeUnlockDelayMs: Long get() = 500L
    val initialPinCode: String get() = ""
    val stopUiStateFlowTimeoutMillis: Long get() = 5_000L

    val errorMapper: (error: PinCodeManagerImpl.Error) -> Throwable
}