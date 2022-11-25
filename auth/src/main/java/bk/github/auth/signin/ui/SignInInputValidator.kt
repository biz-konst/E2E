package bk.github.auth.signin.ui

import java.util.regex.Pattern

interface SignInInputValidator {
    suspend operator fun invoke(value: String): String?

    @Suppress("MemberVisibilityCanBePrivate")
    open class Concrete(conditions: List<Pair<String, String>>? = null) : SignInInputValidator {

        protected val conditions: LinkedHashMap<Pattern, String> = linkedMapOf()

        init {
            conditions?.forEach { p -> addCondition(p.first, p.second) }
        }

        override suspend fun invoke(value: String): String? =
            conditions.firstOrNull { !it.matcher(value).matches() }?.value

        fun addCondition(pattern: String, message: String): SignInInputValidator =
            apply { conditions[Pattern.compile(pattern)] = message }

        private fun <K, V> LinkedHashMap<K, V>.firstOrNull(predicate: (K) -> Boolean): Map.Entry<K, V>? {
            if (!isEmpty()) for (entry in this) if (predicate(entry.key)) return entry
            return null
        }
    }

}