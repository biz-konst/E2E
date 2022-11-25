package bk.github.auth.pincode.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import bk.github.auth.R
import bk.github.auth.databinding.PinCodeFragmentBinding
import bk.github.auth.pincode.PinCodeFeatureConfig
import bk.github.auth.pincode.views.applyLength
import bk.github.auth.pincode.views.forceAppend
import bk.github.auth.pincode.views.removeLast
import bk.github.auth.utils.TimeoutShowHelper
import bk.github.tools.doOnClick
import bk.github.tools.launchWhenStarted
import bk.github.tools.requireMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Suppress("MemberVisibilityCanBePrivate")
abstract class PinCodeFrag : Fragment() {

    companion object {
        private const val CHAR_0 = '0'
        private const val CHAR_1 = '1'
        private const val CHAR_2 = '2'
        private const val CHAR_3 = '3'
        private const val CHAR_4 = '4'
        private const val CHAR_5 = '5'
        private const val CHAR_6 = '6'
        private const val CHAR_7 = '7'
        private const val CHAR_8 = '8'
        private const val CHAR_9 = '9'

        const val TAG = "Auth.PinCodeFragment"
        private const val PIN_CODE_ERROR_HELPER_STATE = "PinCodeErrorHelper@State"

        private val ATTRS = intArrayOf(R.attr.authPinCodePinViewLayout)
    }

    interface PinCodeViewModel {
        val uiState: Flow<UiState>
        val config: PinCodeFeatureConfig

        fun checkPinCode(pinCode: String)
        fun queryPinCode()
        fun clearPinCodeFailure(failure: Throwable)

        data class UiState(
            val status: PinCodeStatus,
            val pinCodeLength: Int = 0,
            val attemptsLeft: Int = 0,
            val pinCodeLifetime: Long = Long.MAX_VALUE,
            val failure: Throwable? = null,
            val queryTimeout: Long = 0,
        ) {
            inline val inputLocked get() = pinCodeLength == 0 || pinCodeLifetime <= 0 || attemptsLeft == 0
            inline val queryLocked get() = queryTimeout > 0
        }

        enum class PinCodeStatus { Input, Request, Checking, Accepted }
    }

    class PinCodeVMStub : ViewModel(), PinCodeViewModel {
        private val _uiState = MutableStateFlow(
            PinCodeViewModel.UiState(
                status = PinCodeViewModel.PinCodeStatus.Input,
                pinCodeLength = 4,
                attemptsLeft = 3
            )
        )
        override val uiState = _uiState.asStateFlow()

        override val config = object : PinCodeFeatureConfig {
            override val formatter = object : PinCodeFormatter {}
        }

        override fun checkPinCode(pinCode: String) {
            if (pinCode.length == _uiState.value.pinCodeLength) {
                _uiState.update { it.copy(status = PinCodeViewModel.PinCodeStatus.Checking) }
                viewModelScope.launch {
                    //delay(1000)
                    _uiState.update {
                        it.copy(
                            status = PinCodeViewModel.PinCodeStatus.Input,
                            failure = Exception("Error pin-code")
                        )
                    }
                }
            }
        }

        override fun queryPinCode() {}

        override fun clearPinCodeFailure(failure: Throwable) {
            _uiState.update { it.copy(failure = null) }
        }

    }

    open val pinCodeViewModel: PinCodeVMStub by viewModels {
        viewModelFactory {
            initializer { createViewModel() }
        }
    }

    lateinit var pinCodeBinding: PinCodeFragmentBinding

    inline val config get() = pinCodeViewModel.config

    private var pinViewId = R.layout.pin_code_pin_view
    private val pinGrid by lazy { pinCodeBinding.pinGrid }

    private var lastPinCodeFailure: Throwable? = null

    private val pinCodeErrorHelper = PinCodeErrorHelper()
    private val textShowHelper = TextShowHelper()

    private var pinCodeLocked = false

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        pinCodeBinding = PinCodeFragmentBinding.bind(requireView().findViewById(R.id.codeContainer))
        initAttrs(pinCodeBinding.codeContainer.context)
        restoreState(savedInstanceState)
        initViews()
        bindViewModel()
    }

    abstract fun createViewModel(): PinCodeVMStub

    open fun navigateToPinCodeAccept() {}

    open fun onInput(state: PinCodeViewModel.UiState) {
        showCheckProgress(false)
        showQueryProgress(false)
        setInputEnabled(!state.inputLocked)
        setQueryEnabled(!state.queryLocked)
        doPinCodeError(state.failure)
    }

    open fun onRequest(state: PinCodeViewModel.UiState) {
        setInputEnabled(false)
        setQueryEnabled(false)
        showCheckProgress(false)
        showQueryProgress(true)
    }

    open fun onChecking(state: PinCodeViewModel.UiState) {
        setInputEnabled(false)
        setQueryEnabled(false)
        showQueryProgress(false)
        showCheckProgress(true)
    }

    open fun onAccepted(state: PinCodeViewModel.UiState) {
        showCheckProgress(false)
        showQueryProgress(false)
        navigateToPinCodeAccept()
    }

    open fun onPinCodeError(failure: Throwable) {
        showPinCodeError(failure)
        pinCodeViewModel.clearPinCodeFailure(failure)
    }

    open fun showCheckProgress(flag: Boolean) {
        setPinCodeProgressText(if (flag) formatPinCodeProgress() else null)
    }

    open fun showQueryProgress(flag: Boolean) {}

    open fun showPinCodeError(failure: Throwable) {
        disablePinCode(config.pinCodeUnlockDelayMs)
        pinCodeErrorHelper.reset(formatPinCodeError(failure), config.errorCleaningDelayMs)
    }

    open fun showQueryTimeout(timeout: Long) {
        setTimeoutOnQueryButton(if (timeout == 0L) null else formatQueryTimeout(timeout))
    }

    open fun setBackspaceAvailable(flag: Boolean = pinGrid.code.isNotEmpty()) {
        pinCodeBinding.actionBackspace.apply { if (isVisible != flag) isVisible = flag }
    }

    open fun formatPinCodeHelper(attemptsLeft: Int, lifetime: Long): String? = null

    open fun formatPinCodeProgress(): String? = null

    open fun formatPinCodeError(failure: Throwable): String? = failure.requireMessage

    open fun formatQueryTimeout(timeout: Long): String? = null

    fun queryPinCode() {
        pinCodeViewModel.queryPinCode()
    }

    fun checkPinCode() {
        pinCodeViewModel.checkPinCode(obtainPinCode())
    }

    fun enterChar(char: Char) {
        if (!pinCodeLocked) pinGrid.forceAppend(char)
    }

    fun enterBackspace() {
        pinGrid.removeLast()
    }

    fun obtainPinCode() = pinGrid.code

    fun setPinCode(value: String) {
        pinGrid.code = value
    }

    fun setNumPadVisible(visible: Boolean) {
        pinCodeBinding.numPadLayout.isVisible = visible
    }

    fun setInputEnabled(enabled: Boolean) {
        with(pinCodeBinding) {
            pinGrid.isEnabled = enabled
            numPadLayout.enable(enabled)
            if (enabled) setBackspaceAvailable()
        }
    }

    fun setQueryEnabled(enabled: Boolean) {
        pinCodeBinding.actionQuery.isEnabled = enabled
    }

    fun setPinCodeHelperText(text: String?, showImmediate: Boolean = true) {
        textShowHelper.helperText = text
        if (showImmediate) textShowHelper.showText(pinCodeBinding)
    }

    fun setPinCodeProgressText(text: String?, showImmediate: Boolean = true) {
        textShowHelper.progressText = text
        if (showImmediate) textShowHelper.showText(pinCodeBinding)
    }

    fun setPinCodeErrorText(text: String?, showImmediate: Boolean = true) {
        pinGrid.failed = !text.isNullOrEmpty()
        textShowHelper.errorText = text
        if (showImmediate) textShowHelper.showText(pinCodeBinding)
    }

    private fun initAttrs(context: Context) {
        with(context.theme.obtainStyledAttributes(ATTRS)) {
            pinViewId = getResourceId(0, pinViewId)
            recycle()
        }
    }

    private fun initViews() {
        with(pinCodeBinding) {
            pinGrid.setOnTextChangedListener(::onPinCodeChanged)
            actionNum0.doOnClick { enterChar(CHAR_0) }
            actionNum1.doOnClick { enterChar(CHAR_1) }
            actionNum2.doOnClick { enterChar(CHAR_2) }
            actionNum3.doOnClick { enterChar(CHAR_3) }
            actionNum4.doOnClick { enterChar(CHAR_4) }
            actionNum5.doOnClick { enterChar(CHAR_5) }
            actionNum6.doOnClick { enterChar(CHAR_6) }
            actionNum7.doOnClick { enterChar(CHAR_7) }
            actionNum8.doOnClick { enterChar(CHAR_8) }
            actionNum9.doOnClick { enterChar(CHAR_9) }
            actionBackspace.doOnClick { enterBackspace() }
            actionQuery.doOnClick { queryPinCode() }
            setBackspaceAvailable()
        }
    }

    private fun bindViewModel() {
        launchWhenStarted {
            pinCodeViewModel.uiState.apply {
                collect { uiStateChanged(it) }
            }
        }
    }


    private fun uiStateChanged(state: PinCodeViewModel.UiState) {
        setPinCodeLength(state.pinCodeLength)
        when (state.status) {
            PinCodeViewModel.PinCodeStatus.Input -> onInput(state)
            PinCodeViewModel.PinCodeStatus.Request -> onRequest(state)
            PinCodeViewModel.PinCodeStatus.Checking -> onChecking(state)
            PinCodeViewModel.PinCodeStatus.Accepted -> onAccepted(state)
        }
        setPinCodeHelperText(formatPinCodeHelper(state.attemptsLeft, state.pinCodeLifetime))
        showQueryTimeout(state.queryTimeout)
        textShowHelper.showText(pinCodeBinding)
    }

    private fun onPinCodeChanged(view: View, code: String) {
        clearPinCodeError()
        setBackspaceAvailable(code.isNotEmpty())
        checkPinCode()
    }

    private fun doPinCodeError(failure: Throwable?) {
        if (lastPinCodeFailure != failure) {
            lastPinCodeFailure = failure
            if (failure != null) onPinCodeError(failure)
        }
    }

    private fun clearPinCodeError() {
        pinCodeErrorHelper.reset()
    }

    private fun disablePinCode(unlockDelay: Long) {
        if (unlockDelay > 0) {
            pinCodeLocked = true
            pinGrid.postDelayed({ pinCodeLocked = false }, unlockDelay)
        }
    }

    private fun setPinCodeLength(length: Int) {
        if (pinGrid.length != length) pinGrid.applyLength(length, pinViewId)
    }

    private fun setTimeoutOnQueryButton(text: String?) {
        TimeoutShowHelper.showTimeout(pinCodeBinding.actionQuery, text ?: return)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle(PIN_CODE_ERROR_HELPER_STATE, pinCodeErrorHelper.writeState())
    }

    private fun restoreState(savedInstanceState: Bundle?) {
        pinCodeErrorHelper.readState(savedInstanceState?.getBundle(PIN_CODE_ERROR_HELPER_STATE))
    }

    private fun ViewGroup.enable(enabled: Boolean) {
        repeat(childCount) { getChildAt(it).isEnabled = enabled }
    }

    private inner class PinCodeErrorHelper : Runnable {
        private var startTime: Long = 0
        private var errorText: String? = null

        override fun run() {
            clearPinCodeError()
        }

        fun writeState() = bundleOf("startTime" to startTime, "errorText" to errorText)

        fun readState(savedState: Bundle?) {
            startTime = savedState?.getLong("startTime") ?: 0
            errorText = savedState?.getString("errorText")
            reset(errorText, startTime - System.currentTimeMillis())
        }

        fun reset(errorText: String? = null, delay: Long = 0) {
            pinGrid.removeCallbacks(this)
            this.startTime = delay + System.currentTimeMillis()
            this.errorText = errorText
            setPinCodeErrorText(errorText)
            if (delay > 0 && errorText != null) {
                pinGrid.postDelayed(this, delay)
            }
        }
    }

    private class TextShowHelper {
        var helperText: String? = null
        var progressText: String? = null
        var errorText: String? = null

        fun showText(binding: PinCodeFragmentBinding) {
            if (errorText != null) {
                binding.errorText.text = errorText
                binding.errorText.isVisible = true
                binding.progressText.isVisible = false
                binding.helperText.isVisible = false
            } else {
                binding.errorText.isVisible = false
                if (progressText != null) {
                    binding.progressText.text = progressText
                    binding.progressText.isVisible = true
                    binding.helperText.isVisible = false
                } else {
                    binding.progressText.isVisible = false
                    binding.helperText.text = helperText
                    binding.helperText.isVisible = true
                }
            }
        }
    }

}