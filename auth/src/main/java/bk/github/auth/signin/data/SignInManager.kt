package bk.github.auth.signin.data

import kotlinx.coroutines.flow.Flow

interface SignInManager {
    fun observeAvailableLogins(): Flow<Array<String>>
    suspend fun signIn(nickname: String, password: String): Result<Unit>
}