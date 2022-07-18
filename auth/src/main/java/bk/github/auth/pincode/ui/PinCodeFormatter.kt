package bk.github.auth.pincode.ui

import bk.github.auth.pincode.ui.PinCodeViewModel.UiState.PinCodeStatus
import bk.github.auth.pincode.ui.PinCodeViewModel.UiState.RequestStatus

@Suppress("unused")
interface PinCodeFormatter {
    fun formatHelperText(
        status: PinCodeStatus,
        attemptNumber: Int,
        numberOfAttempts: Int,
        lifetime: Long
    ): String? = null

    fun formatProgressText(pinCodeStatus: PinCodeStatus, requestStatus: RequestStatus): String? =
        null

    fun formatRequestDelayText(requestTimeout: Long): String? = null
}