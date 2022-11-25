package bk.github.auth.signin.ui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Suppress("MemberVisibilityCanBePrivate")
class SpinnerAdapter<T>(
    context: Context,
    resource: Int,
    textViewResourceId: Int = 0,
    objects: List<T> = mutableListOf(),
    val excludeFilter: Boolean = true
) : ArrayAdapter<T>(context, resource, textViewResourceId, objects) {

    companion object {
        private const val NO_FILTER = -1
    }

    private val filter by lazy { ItemFilter() }
    private var filtered: Int = NO_FILTER

    // ArrayAdapter

    override fun getCount(): Int =
        super.getCount().let { if (filtered in 0 until it) it - 1 else it }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
        withFilter(position) { super.getView(it, convertView, parent) }

    override fun getItem(position: Int): T? = withFilter(position) { super.getItem(it) }

    override fun getItemId(position: Int): Long = withFilter(position) { super.getItemId(it) }

    override fun getItemViewType(position: Int): Int =
        withFilter(position) { super.getItemViewType(it) }

    override fun isEnabled(position: Int): Boolean =
        withFilter(position) { super.isEnabled(it) }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View =
        withFilter(position) { super.getDropDownView(it, convertView, parent) }

    override fun notifyDataSetChanged() {
        filtered = NO_FILTER
        super.notifyDataSetChanged()
    }

    override fun notifyDataSetInvalidated() {
        filtered = NO_FILTER
        super.notifyDataSetInvalidated()
    }

    suspend fun submitItems(items: List<T>?, onComplete: (suspend () -> Unit)? = null) {
        setNotifyOnChange(false)
        try {
            clear()
            items?.let { addAll(it) }
        } finally {
            withContext(Dispatchers.Main.immediate) {
                notifyDataSetChanged()
                onComplete?.invoke()
            }
        }
    }

    private inline fun <R> withFilter(position: Int, block: (Int) -> R): R {
        return if (filtered in 0..position) {
            val was = filtered
            filtered = NO_FILTER
            try {
                block(position + 1)
            } finally {
                filtered = was
            }
        } else {
            block(position)
        }
    }

    // Filterable

    override fun getFilter(): Filter = if (excludeFilter) filter else super.getFilter()

    private fun findItem(constraint: String): Int {
        for (i in 0 until super.getCount()) {
            if (super.getItem(i).toString().equals(constraint, ignoreCase = true)) {
                return i
            }
        }
        return NO_FILTER
    }

    private fun setFiltered(value: Int) {
        filtered = value
        if (isEmpty) {
            super.notifyDataSetInvalidated()
        } else {
            super.notifyDataSetChanged()
        }
    }

    private inner class ItemFilter : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults =
            FilterResults().apply {
                values =
                    if (constraint.isNullOrEmpty()) NO_FILTER else findItem(constraint.toString())
                count = if (values == NO_FILTER) 0 else 1
            }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            setFiltered((results?.values as? Int) ?: NO_FILTER)
        }
    }

}


