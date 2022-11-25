package bk.github.auth.signin.data

import bk.github.auth.signin.data.model.SignInData
import bk.github.auth.signin.data.model.SignInState
import kotlinx.coroutines.flow.Flow

interface SignInDataSource {
    fun observeServerList(): Flow<List<String>>
    fun observeSignInState(server: String?): Flow<SignInState>
    suspend fun signIn(data: SignInData): Result<*>
    suspend fun addAvailableNickname(nickname: String, server: String?): Result<*>
}