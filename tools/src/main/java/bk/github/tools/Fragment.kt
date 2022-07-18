@file:Suppress("unused", "NOTHING_TO_INLINE")

package bk.github.tools

import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.Flow

inline fun Fragment.shortToast(text: String?) =
    text?.let { context?.toast(text, Toast.LENGTH_SHORT) }

inline fun Fragment.longToast(text: String?) =
    text?.let { context?.toast(text, Toast.LENGTH_LONG) }

inline fun <T> Fragment.observeOnLifecycle(
    flow: Flow<T>,
    lifecycleOwner: LifecycleOwner = viewLifecycleOwner,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    noinline block: (T) -> Unit
) = flow.collectOnLifecycle(lifecycleOwner, state, block)

inline fun <T> Fragment.observeOnLifecycle(
    flow: Flow<T>,
    lifecycleOwner: LifecycleOwner = viewLifecycleOwner,
    state: Lifecycle.State = Lifecycle.State.STARTED,
) = flow.collectOnLifecycle(lifecycleOwner, state) { }

fun <T> Fragment.onValueChanged(tag: String, value: T, action: (T) -> Unit) {
    val manager = (requireView().getTag(R.id.tag_observed_value_manager) as? ObservedValueManager)
        ?: ObservedValueManager().also { requireView().setTag(R.id.tag_observed_value_manager, it) }
    if (!manager.compareAndSet(tag, value)) action(value)
}

private class ObservedValueManager {
    private val map = linkedMapOf<String, Any?>()

    fun compareAndSet(tag: String, value: Any?): Boolean {
        val old = map[tag]
        if (old == value) return true

        map[tag] = value
        return false
    }

}
