package bk.github.auth.signin.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import bk.github.auth.R
import bk.github.auth.databinding.SignInFragmentBinding
import bk.github.auth.signin.SignInFeatureConfig

@Suppress("unused", "MemberVisibilityCanBePrivate")
abstract class SignInFragment : Fragment() {

    open val config: SignInFeatureConfig by lazy { signInViewModel.config }

    val signInViewModel: SignInViewModel by viewModels(
        ownerProducer = ::getViewModelOwner, factoryProducer = ::getViewModelFactory
    )

    lateinit var signInBinding: SignInFragmentBinding private set


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        signInBinding = SignInFragmentBinding.bind(view.findViewById(R.id.signInContainer))
        signInBinding.apply {
//            val helperTextView =
//                nicknameInputLayout.findViewById<AppCompatTextView>(textinput_helper_text)
//            helperTextView.textAlignment = View.TEXT_ALIGNMENT_CENTER
            passwordInputLayout.error = "error text, error text, error text, error text"
//            val errorView =
//                passwordInputLayout.findViewById<AppCompatTextView>(textinput_helper_text)
//            errorView.textAlignment = View.TEXT_ALIGNMENT_CENTER
//            serverEdit.keyListener = null
//            serverEdit.inputType = EditorInfo.TYPE_NULL

            serverEdit.setAdapter(getAdapter())
        }
    }

    abstract fun getViewModelFactory(): ViewModelProvider.Factory

    open fun getViewModelOwner(): ViewModelStoreOwner = this

    private fun getAdapter(): AdapterWrapper {
        return AdapterWrapper(
            ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line
            ).apply {
                add("1 item")
                add("2 item")
            }
        )
    }

    class AdapterWrapper(adapter: BaseAdapter) : Filter(), ListAdapter by adapter,
        Filterable {

        //var wrapped: BaseAdapter = adapter

        private val emptyResult = FilterResults()

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            return emptyResult
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            //wrapped.notifyDataSetChanged()
        }

        override fun getFilter(): Filter = this

    }

    class MyAdapter<T>(context: Context, resource: Int) : ArrayAdapter<T>(context, resource) {

        private val filter by lazy {
            object : Filter() {
                private val emptyResult = FilterResults()

                override fun performFiltering(constraint: CharSequence?): FilterResults {
                    return emptyResult
                }

                override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                    notifyDataSetInvalidated()
                }

            }
        }

        override fun getFilter(): Filter = filter

    }

}

