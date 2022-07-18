package bk.github.auth.signin.data

interface SignInValidator {
    suspend fun validate(value: String, inputComplete: Boolean): String?
}