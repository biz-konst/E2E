package bk.github.auth.pincode

class WrongPinCodeException @JvmOverloads constructor(
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause)