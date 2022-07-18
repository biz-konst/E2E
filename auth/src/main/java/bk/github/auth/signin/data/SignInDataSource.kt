package bk.github.auth.signin.data

import kotlinx.coroutines.flow.Flow

interface SignInDataSource {
    fun observeAvailableLogins(): Flow<Array<String>>
    suspend fun addAvailableLogin(login: String): Result<String>
    suspend fun signIn(nickname: String, password: String): Result<Any>
}