package com.example.e2e.signin

import bk.github.auth.signin.data.SignInDataSource
import bk.github.auth.signin.data.model.SignInData
import bk.github.auth.signin.data.model.SignInState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

class SignInDataSourceTest : SignInDataSource {

    private val emptySignInState = SignInState(
        server = null,
        availableNicknames = setOf(),
        signInUnlockTime = 0,
        signedIn = false
    )

    private val servers = listOf("Server 1", "Server 2")
    private val signInStates = hashMapOf<String?, SignInState>()
    private val signInState = MutableStateFlow(emptySignInState)

    init {
        var count = 0
        servers.forEach { server ->
            signInStates[server] =
                SignInState(
                    server = server,
                    availableNicknames = setOf("User ${++count}", "User ${++count}"),
                    signInUnlockTime = 0,
                    signedIn = false
                )
        }
    }

    override fun observeServerList(): Flow<List<String>> = flowOf(servers)

    override fun observeSignInState(server: String?): Flow<SignInState> {
        signInState.value = signInStates[server] ?: emptySignInState
        return signInState
    }

    override suspend fun addAvailableNickname(nickname: String, server: String?): Result<*> {
        updateSignInState(server) {
            it.copy(availableNicknames = it.availableNicknames + nickname)
        }
        return Result.success(Unit)
    }

    override suspend fun signIn(data: SignInData): Result<*> {
        val result = if (data.nickname != "1")
            Result.failure<Nothing>(IllegalArgumentException("User not found"))
        else if (data.password != "1")
            Result.failure<Nothing>(IllegalArgumentException("Password failure"))
        else Result.success(Unit)
        return result.also {
            updateSignInState(data.server) {
                if (result.isSuccess) {
                    it.copy(signedIn = true)
                } else {
                    it.copy(signInUnlockTime = System.currentTimeMillis() + 10000L)
                }
            }
        }
    }

    private fun updateSignInState(server: String?, block: (SignInState) -> SignInState) {
        signInState.value = block(signInState.value)
        signInStates[server]?.let { signInStates[it.server] = signInState.value }
    }

}