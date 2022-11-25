package com.example.e2e.pincode

import android.app.Application
import bk.github.auth.pincode.ui.PinCodeFormatter
import bk.github.auth.pincode.ui.PinCodeViewModel
import com.example.e2e.R
import com.example.e2e.TimeoutFormatter

class PinCodeFormatterImpl(private val app: Application) : PinCodeFormatter {

    override fun formatHelperText(
        status: PinCodeViewModel.UiState.PinCodeStatus,
        attemptNumber: Int,
        numberOfAttempts: Int,
        lifetime: Long
    ): String {
        val attemptsLeft = numberOfAttempts - attemptNumber
        return when {
            status == PinCodeViewModel.UiState.PinCodeStatus.Accepted ->
                app.getString(R.string.auth_code_pin_code_accepted)
            attemptsLeft <= 0 || lifetime <= 0 ->
                app.getString(R.string.auth_code_need_pin_code_request)
            lifetime < Long.MAX_VALUE && numberOfAttempts < Int.MAX_VALUE ->
                app.resources.getQuantityString(
                    R.plurals.auth_code_attempts_left_with_lifetime,
                    attemptsLeft,
                    attemptsLeft,
                    numberOfAttempts,
                    TimeoutFormatter.format(lifetime, app)
                )
            lifetime < Long.MAX_VALUE ->
                app.getString(
                    R.string.auth_code_attempts_lifetime,
                    TimeoutFormatter.format(lifetime, app)
                )
            numberOfAttempts < Int.MAX_VALUE && attemptNumber > 0 ->
                app.resources.getQuantityString(
                    R.plurals.auth_code_attempts_left,
                    attemptsLeft,
                    attemptsLeft,
                    numberOfAttempts
                )
            else -> ""
        }
    }

    override fun formatProgressText(
        pinCodeStatus: PinCodeViewModel.UiState.PinCodeStatus,
        requestStatus: PinCodeViewModel.UiState.RequestStatus
    ): String? {
        return when {
            pinCodeStatus == PinCodeViewModel.UiState.PinCodeStatus.Checking ->
                app.getString(R.string.auth_code_checking_pin_code_progress)
            requestStatus == PinCodeViewModel.UiState.RequestStatus.Performing ->
                app.getString(R.string.auth_code_request_pin_code_progress)
            else -> null
        }
    }

    override fun formatRequestDelayText(requestTimeout: Long): String? {
        return if (requestTimeout <= 0 || requestTimeout == Long.MAX_VALUE) {
            null
        } else {
            app.getString(
                R.string.auth_code_request_pin_code_delay,
                TimeoutFormatter.format(requestTimeout, app)
            )
        }
    }

}