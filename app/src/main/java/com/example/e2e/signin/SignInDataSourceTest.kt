package com.example.e2e.signin

import bk.github.auth.signin.data.SignInDataSource
import bk.github.auth.signin.data.model.SignInData
import bk.github.auth.signin.data.model.SignInState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SignInDataSourceTest : SignInDataSource {

    private val availableLogins = mutableSetOf<String>()

    override fun observeServerList(): Flow<List<String>> = flowOf(emptyList())

    override fun observeSignInState(server: String?): Flow<SignInState> = flowOf(
        SignInState(
            server = null,
            availableNicknames = availableLogins.toList(),
            signInUnlockTime = 0
        )
    )

    override suspend fun addAvailableNickname(nickname: String, server: String?): Result<*> {
        return Result.success(Unit)
    }

    override suspend fun signIn(data: SignInData): Result<*> {
        return Result.success(Unit)
    }

}