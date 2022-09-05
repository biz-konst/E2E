package bk.github.auth.signin.ui

interface SignInInputValidator {
    suspend operator fun invoke(value: String): String?
}