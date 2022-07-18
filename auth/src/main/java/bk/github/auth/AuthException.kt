package bk.github.auth

class AuthException @JvmOverloads constructor(
    val code: Int, message: String? = null, cause: Throwable? = null
) : Exception(message, cause)