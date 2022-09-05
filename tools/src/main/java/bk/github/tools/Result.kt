@file:Suppress("unused")

package bk.github.tools

class Loading private constructor(message: String?) : Throwable(message = message) {
    companion object {
        fun <T> result(message: String? = null) = Result.failure<T>(Loading(message))
    }
}

inline val Result<*>.isLoading: Boolean get() = exceptionOrNull() is Loading

@PublishedApi
internal inline val Result<*>.loadingMessage: String?
    get() = (exceptionOrNull() as? Loading)?.message

inline fun <R, T> Result<T>.fold(
    onLoading: (message: String?) -> R,
    onSuccess: (value: T) -> R,
    onFailure: (exception: Throwable) -> R
): R {
    return if (isLoading) onLoading(loadingMessage) else fold(onSuccess, onFailure)
}

inline fun <T> Result<T>.onLoading(action: (message: String?) -> Unit): Result<T> {
    if (isLoading) action(loadingMessage)
    return this
}

fun <T> Result<T>.failureOrNull(): Throwable? {
    return when (val exception = exceptionOrNull()) {
        null, is Loading -> null
        else -> exception
    }
}
