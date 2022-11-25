package bk.github.auth.signin.data

import bk.github.auth.signin.data.model.SignInData
import bk.github.auth.signin.data.model.SignInState
import kotlinx.coroutines.flow.Flow

interface SignInManager {

    fun observeServerList(): Flow<List<String>>
    fun observeSignInState(server: String?): Flow<SignInState>
    suspend fun signIn(data: SignInData): Result<*>
    fun signUpNeeded(result: Result<*>): Boolean

    @Suppress("unused", "MemberVisibilityCanBePrivate")
    open class Default(protected val source: SignInDataSource) : SignInManager {
        override fun observeServerList() = source.observeServerList()

        override fun observeSignInState(server: String?) = source.observeSignInState(server)

        override suspend fun signIn(data: SignInData): Result<*> {
            return source.signIn(data).onSuccess { signedIn(data) }
        }

        override fun signUpNeeded(result: Result<*>): Boolean = false

        protected suspend fun signedIn(data: SignInData) {
            source.addAvailableNickname(data.nickname, data.server)
        }
    }

}