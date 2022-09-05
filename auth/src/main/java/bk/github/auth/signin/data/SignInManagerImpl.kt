package bk.github.auth.signin.data

import bk.github.auth.AUTH_BASE_ERROR_CODE
import bk.github.auth.signin.data.model.SignInData
import kotlinx.coroutines.flow.Flow

@Suppress("unused", "MemberVisibilityCanBePrivate")
open class SignInManagerImpl(
    protected val source: SignInDataSource
) : SignInManager {

    companion object {
        private const val ERROR_CODE = AUTH_BASE_ERROR_CODE + 1000

        const val LOGIN_NOT_FOUND = ERROR_CODE + 1
    }

    override fun observeServerList(): Flow<List<String>> = source.observeServerList()

    override fun observeSignInState(server: String?) = source.observeSignInState(server)

    override suspend fun signIn(data: SignInData): Result<*> {
        return source.signIn(data)
            .fold(
                onSuccess = {
                    signedIn(data.nickname, it)
                    Result.success(Unit)
                },
                onFailure = {
                    Result.failure(
//                        if (it is AuthException && it.code == LOGIN_NOT_FOUND) {
//                            LoginNotFoundException(it.message)
//                        } else {
//                            it
//                        }
                        it
                    )
                }
            )
    }

    open suspend fun signedIn(nickname: String, token: Any?) {
        //source.addAvailableNickname(nickname)
    }

}