package com.example.e2e

import bk.github.auth.AuthException
import bk.github.auth.signin.data.SignInDataSource
import bk.github.auth.signin.data.SignInManagerImpl
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import java.util.concurrent.TimeoutException

class SignInDataSourceTest : SignInDataSource {

    private val availableLogins = mutableSetOf<String>()
    private val channel = Channel<Array<String>>()

    override fun observeAvailableLogins() = channel.consumeAsFlow()

    override suspend fun addAvailableLogin(login: String): Result<String> {
        availableLogins += login
        channel.send(availableLogins.toTypedArray())
        return Result.success(login)
    }

    private var counter = 0
    private val accounts = mutableMapOf<String, String>()

    override suspend fun signIn(nickname: String, password: String): Result<Any> {
        delay(1000)
        counter++
        return when {
            accounts.contains(nickname) -> {
                if (accounts[nickname] != password) {
                    Result.failure(Exception("Invalid password"))
                } else {
                    Result.success(Unit)
                }
            }
            counter % 3 == 2 -> {
                accounts[nickname] = password
                Result.success(Unit)
            }
            counter % 2 == 1 ->
                Result.failure(TimeoutException("Server not responded"))
            else -> Result.failure(
                AuthException(
                    //code = SignInManagerImpl.LOGIN_NOT_FOUND,
                    message = "Login not found"
                )
            )
        }
    }

}