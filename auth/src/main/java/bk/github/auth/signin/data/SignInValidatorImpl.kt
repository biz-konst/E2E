package bk.github.auth.signin.data

import android.util.Log
import java.util.regex.Pattern

@Suppress("MemberVisibilityCanBePrivate")
open class SignInValidatorImpl(conditions: List<Pair<String, String>>? = null) : SignInValidator {

    protected val conditions: LinkedHashMap<Pattern, String> = linkedMapOf<Pattern, String>()
        .also { conditions?.forEach { p -> addCondition(p.first, p.second) } }

    override suspend fun validate(value: String, inputComplete: Boolean): String? {
        return conditions.firstOrNull { !it.matcher(value).matches() }?.value
    }

    fun addCondition(pattern: String, message: String): SignInValidator =
        apply { conditions[Pattern.compile(pattern)] = message }

    private fun <K, V> LinkedHashMap<K, V>.firstOrNull(predicate: (K) -> Boolean): Map.Entry<K, V>? {
        if (!isEmpty()) for (entry in this) if (predicate(entry.key)) return entry
        return null
    }

}