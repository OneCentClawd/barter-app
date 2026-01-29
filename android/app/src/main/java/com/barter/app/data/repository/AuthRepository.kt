package com.barter.app.data.repository

import com.barter.app.data.local.TokenManager
import com.barter.app.data.model.*
import com.barter.app.data.remote.ApiService
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    suspend fun login(username: String, password: String): Result<AuthResponse> {
        return try {
            val response = apiService.login(LoginRequest(username, password))
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data!!
                tokenManager.saveAuthData(
                    token = data.token,
                    userId = data.userId,
                    username = data.username,
                    nickname = data.nickname,
                    avatar = data.avatar
                )
                Result.Success(data)
            } else {
                Result.Error(response.body()?.message ?: "登录失败")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "网络错误")
        }
    }

    suspend fun register(
        username: String,
        email: String,
        password: String,
        nickname: String?
    ): Result<AuthResponse> {
        return try {
            val response = apiService.register(RegisterRequest(username, email, password, nickname))
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data!!
                tokenManager.saveAuthData(
                    token = data.token,
                    userId = data.userId,
                    username = data.username,
                    nickname = data.nickname,
                    avatar = data.avatar
                )
                Result.Success(data)
            } else {
                Result.Error(response.body()?.message ?: "注册失败")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "网络错误")
        }
    }

    suspend fun logout() {
        tokenManager.clearAuthData()
    }

    suspend fun isLoggedIn(): Boolean {
        return tokenManager.token.first() != null
    }

    fun getToken() = tokenManager.token
    fun getUserId() = tokenManager.userId
    fun getUsername() = tokenManager.username
    fun getNickname() = tokenManager.nickname
    fun getAvatar() = tokenManager.avatar
}
