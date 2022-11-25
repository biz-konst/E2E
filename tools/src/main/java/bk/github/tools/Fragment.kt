@file:Suppress("unused", "NOTHING_TO_INLINE")

package bk.github.tools

import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

inline fun Fragment.shortToast(text: String?) =
    text?.let { context?.toast(text, Toast.LENGTH_SHORT) }

inline fun Fragment.longToast(text: String?) =
    text?.let { context?.toast(text, Toast.LENGTH_LONG) }

fun Fragment.launchWhenStarted(block: suspend CoroutineScope.() -> Unit) =
    lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED, block)
    }


