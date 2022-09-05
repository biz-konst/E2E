package bk.github.auth.pincode.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import bk.github.auth.R
import bk.github.auth.databinding.PinCodeFragmentBinding
import bk.github.auth.pincode.PinCodeFeatureConfig
import bk.github.auth.pincode.ui.PinCodeViewModel.UiState
import bk.github.auth.pincode.ui.PinCodeViewModel.UiState.*
import bk.github.auth.pincode.views.applyLength
import bk.github.auth.pincode.views.forceAppend
import bk.github.auth.pincode.views.removeLast
import bk.github.tools.observeOnLifecycle
import bk.github.tools.requireMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@Suppress("unused", "MemberVisibilityCanBePrivate")
abstract class PinCodeFragment : Fragment() {

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

        private val ATTRS = intArrayOf(R.attr.authPinCodePinViewLayout)
    }

    interface Animation {
        fun animatePinCode(target: View, failed: Boolean)
        fun animateHelperText(target: TextView, text: String?, visible: Boolean)
        fun animateErrorText(target: TextView, text: String?, visible: Boolean)
        fun animateProgressText(target: TextView, text: String?, visible: Boolean)
        fun animateDelayText(target: TextView, text: String?, visible: Boolean)
        fun cancelPinCodeAnimation()
        fun cancelErrorTextAnimation()
        fun cancelHelperTextAnimation()
        fun cancelProgressTextAnimation()
        fun cancelDelayTextAnimation()
    }

    open val config: PinCodeFeatureConfig by lazy { pinCodeViewModel.config }

    val pinCodeViewModel: PinCodeViewModel by viewModels(
        ownerProducer = ::getViewModelOwner, factoryProducer = ::getViewModelFactory
    )

    lateinit var pinCodeBinding: PinCodeFragmentBinding private set

    private inline val formatter get() = config.formatter
    private inline val animation get() = config.animation
    private inline val pinGrid get() = pinCodeBinding.pinGrid
    private inline val errorCleaningDelayMs get() = config.errorCleaningDelayMs
    private inline val pinCodeUnlockDelayMs get() = config.pinCodeUnlockDelayMs
    private var pinViewId: Int = 0

    private val errorCleaner = Runnable { clearPinCode() }
    private val pinCodeUnlock = Runnable { unlockPinCode() }

    private val pinCodeInputLocked = MutableStateFlow(false)

    private val textShowHelper by lazy { TextShowHelper(pinCodeBinding) }

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pinCodeBinding = PinCodeFragmentBinding.bind(view.findViewById(R.id.codeContainer))
        pinCodeBinding.apply {
            initAttrs(codeContainer.context)
            pinGrid.setOnTextChangedListener(::onPinCodeChanged)
            setupNumPad()
            pinCodeViewModel.apply {
                observeOnLifecycle(uiState.map { it.pinCodeState }.distinctUntilChanged())
                { pinCodeStateChanged(it) }
                observeOnLifecycle(uiState.map { it.requestState }.distinctUntilChanged())
                { requestStateChanged(it) }
                observeOnLifecycle(uiState.combine(pinCodeInputLocked) { s, _ -> uiStateChanged(s) })
            }
        }
    }

    abstract fun getViewModelFactory(): ViewModelProvider.Factory

    abstract fun pinCodeAccept()

    open fun getViewModelOwner(): ViewModelStoreOwner = this

    open fun onPinCodeStateChanged(state: PinCodeState) = Unit

    open fun onRequestStateChanged(state: RequestState) {
        if (isRequestSuccess(state)) {
            pinCodeViewModel.resetRequestState()
            clearPinCodeError()
        }
    }

    open fun onUiStateChanged(state: UiState) {
        if (isPinCodeOverdue(state) && state.pinCodeState.failure == null) {
            clearPinCode()
        }
    }

    open fun isPinCodeEnable(state: UiState): Boolean =
        !(isPinCodeInputLocked() || isPinCodeOverdue(state))
                && state.pinCodeState.status == PinCodeStatus.Input
                && state.requestState.status != RequestStatus.Performing

    open fun isQueryActionVisible(state: UiState): Boolean =
        state.queryLockTimeout < Long.MAX_VALUE ||
                state.requestState.status == RequestStatus.Performing

    open fun isQueryActionEnable(state: UiState): Boolean =
        state.queryLockTimeout <= 0
                && state.requestState.status != RequestStatus.Performing
                && state.pinCodeState.status != PinCodeStatus.Checking

    open fun onPinCodeError(error: Throwable): Boolean {
        setPinCodeError(error.requireMessage)
        return false
    }

    open fun onRequestError(error: Throwable) = true

    open fun onShowProgress(state: UiState) =
        state.pinCodeState.length == 0
                || state.pinCodeState.status == PinCodeStatus.Checking
                || state.requestState.status == RequestStatus.Performing

    fun isPinCodeInputLocked() = pinCodeInputLocked.value

    fun setPinCode(value: String) {
        pinGrid.code = value
        checkPinCode(value)
    }

    private fun initAttrs(context: Context) {
        with(context.theme.obtainStyledAttributes(ATTRS)) {
            pinViewId = getResourceId(0, R.layout.pin_code_pin_view)
            recycle()
        }
    }

    private fun setupNumPad() {
        pinCodeBinding.apply {
            actionNum0.setOnClickListener { enterChar(CHAR_0) }
            actionNum1.setOnClickListener { enterChar(CHAR_1) }
            actionNum2.setOnClickListener { enterChar(CHAR_2) }
            actionNum3.setOnClickListener { enterChar(CHAR_3) }
            actionNum4.setOnClickListener { enterChar(CHAR_4) }
            actionNum5.setOnClickListener { enterChar(CHAR_5) }
            actionNum6.setOnClickListener { enterChar(CHAR_6) }
            actionNum7.setOnClickListener { enterChar(CHAR_7) }
            actionNum8.setOnClickListener { enterChar(CHAR_8) }
            actionNum9.setOnClickListener { enterChar(CHAR_9) }
            actionBackspace.setOnClickListener { enterBackspace() }
            actionQuery.setOnClickListener { queryPinCode() }
            setBackspaceVisibility(pinGrid.code.isNotEmpty())
        }
    }

    private fun enterChar(char: Char) {
        pinGrid.apply {
            forceAppend(char)
            checkPinCode(code)
        }
    }

    private fun enterBackspace() {
        pinGrid.removeLast()
    }

    private fun checkPinCode(text: String) {
        pinGrid.apply {
            if (code.isNotEmpty()) {
                if (pinCodeViewModel.checkPinCode(text)) {
                    lockPinCode()
                }
            }
        }
    }

    private fun queryPinCode() {
        pinCodeViewModel.queryPinCode()
    }

    private fun clearPinCode() {
        pinGrid.clear()
    }

    private fun lockPinCode() {
        pinCodeInputLocked.update { true }
        view?.apply {
            removeCallbacks(pinCodeUnlock)
            postDelayed(pinCodeUnlock, pinCodeUnlockDelayMs)
        }
    }

    private fun unlockPinCode() {
        view?.removeCallbacks(pinCodeUnlock)
        pinCodeInputLocked.update { false }
    }

    private fun clearPinCodeError() {
        if (pinGrid.failed) {
            setPinCodeError(null)
            pinCodeViewModel.resetPinCodeState()
        }
    }

    private fun startClearingPinError() {
        pinGrid.apply {
            removeCallbacks(errorCleaner)
            if (failed) postDelayed(errorCleaner, errorCleaningDelayMs)
        }
    }

    private fun isPinCodeOverdue(state: UiState) =
        state.attemptsLifetime <= 0 || state.attemptNumber >= state.pinCodeState.availableAttempts

    private fun isRequestSuccess(state: RequestState) =
        state.status == RequestStatus.Done && state.failure == null

    private fun onPinCodeChanged(view: View, text: String) {
        setBackspaceVisibility(text.isNotEmpty())
        clearPinCodeError()
    }

    private fun pinCodeStateChanged(state: PinCodeState) {
        setPinCodeLength(state.length)
        if (state.status == PinCodeStatus.Accepted) pinCodeAccept()
        showPinCodeError(state.failure)
        onPinCodeStateChanged(state)
    }

    private fun requestStateChanged(state: RequestState) {
        showRequestError(state.failure)
        onRequestStateChanged(state)
    }

    private fun uiStateChanged(state: UiState) {
        setPinCodeInputEnable(isPinCodeEnable(state))
        setQueryActionVisibility(isQueryActionVisible(state))
        setQueryActionEnable(isQueryActionEnable(state))
        setHelperText(
            formatter.formatHelperText(
                state.pinCodeState.status,
                state.attemptNumber,
                state.pinCodeState.availableAttempts,
                state.attemptsLifetime
            )
        )
        setRequestDelayText(formatter.formatRequestDelayText(state.queryLockTimeout))
        showProgress(onShowProgress(state), state)
        onUiStateChanged(state)
        textShowHelper.showText(animation)
    }

    private fun showPinCodeError(error: Throwable?) {
        if (onPinCodeError(error ?: return)) pinCodeViewModel.resetPinCodeState()
    }

    private fun showRequestError(error: Throwable?) {
        if (onRequestError(error ?: return)) pinCodeViewModel.resetRequestState()
    }

    private fun showProgress(visible: Boolean, state: UiState) {
        pinCodeBinding.checkProgressBar.apply {
            if (isVisible != visible) isInvisible = !visible
            pinGrid.isInvisible = visible
        }

        setProgressText(
            if (visible) {
                formatter.formatProgressText(state.pinCodeState.status, state.requestState.status)
            } else {
                null
            }
        )
    }

    private fun setPinCodeLength(length: Int) {
        if (length != pinGrid.length) pinGrid.applyLength(length, pinViewId)
    }

    private fun setPinCodeInputEnable(enable: Boolean) {
        pinCodeBinding.apply {
            pinGrid.isEnabled = enable
            actionNum0.isEnabled = enable
            actionNum1.isEnabled = enable
            actionNum2.isEnabled = enable
            actionNum3.isEnabled = enable
            actionNum4.isEnabled = enable
            actionNum5.isEnabled = enable
            actionNum6.isEnabled = enable
            actionNum7.isEnabled = enable
            actionNum8.isEnabled = enable
            actionNum9.isEnabled = enable
            actionBackspace.isEnabled = enable
        }
    }

    private fun setBackspaceVisibility(visible: Boolean) {
        pinCodeBinding.actionBackspace.apply {
            if (isVisible != visible) isVisible = visible
        }
    }

    private fun setQueryActionVisibility(visible: Boolean) {
        pinCodeBinding.actionQuery.apply {
            if (isVisible != visible) isVisible = visible
        }
    }

    private fun setQueryActionEnable(enable: Boolean) {
        pinCodeBinding.actionQuery.isEnabled = enable
    }

    private fun setHelperText(text: String?) {
        textShowHelper.helperText = text
    }

    private fun setProgressText(text: String?) {
        textShowHelper.progressText = text
    }

    private fun setPinCodeError(error: String?) {
        val failed = error != null
        if (failed != pinGrid.failed) {
            pinGrid.failed = failed
            animation.animatePinCode(pinGrid, failed)
        }

        startClearingPinError()
        textShowHelper.errorText = error
    }

    private fun setRequestDelayText(text: String?) {
        animation.animateDelayText(pinCodeBinding.requestDelayText, text, text != null)
    }

    private class TextShowHelper(binding: PinCodeFragmentBinding) {

        private val progress = binding.progressText
        private val error = binding.errorText
        private val helper = binding.helperText

        var errorText: String? = null
        var progressText: String? = null
        var helperText: String? = null

        fun showText(animation: Animation) {
            val progressVisible = !progressText.isNullOrEmpty()
            val errorVisible = !progressVisible && !errorText.isNullOrEmpty()
            animation.animateHelperText(helper, helperText, !errorVisible && !progressVisible)
            animation.animateErrorText(error, errorText, errorVisible)
            animation.animateProgressText(progress, progressText, progressVisible)
        }

    }

}