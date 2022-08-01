package bk.github.auth.pincode

import bk.github.auth.AuthException

class WrongPinCodeException(message: String? = null, cause: Throwable? = null) :
    AuthException(message, cause)