package com.barter.app.data.repository

import com.barter.app.data.local.TokenManager
import com.barter.app.data.model.*
import com.barter.app.data.remote.ApiService
import com.barter.app.util.ErrorHandler
import kotlinx.coroutines.flow.first
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
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
                // 优先使用后端返回的错误信息
                val serverMessage = response.body()?.message
                val errorMsg = if (!serverMessage.isNullOrBlank()) {
                    serverMessage
                } else {
                    when (response.code()) {
                        400, 401 -> "用户名或密码错误"
                        404 -> "用户不存在"
                        500 -> "服务器错误，请稍后重试"
                        else -> "登录失败"
                    }
                }
                Result.Error(errorMsg)
            }
        } catch (e: UnknownHostException) {
            Result.Error("无法连接服务器，请检查网络")
        } catch (e: SocketTimeoutException) {
            Result.Error("连接超时，请检查网络后重试")
        } catch (e: ConnectException) {
            Result.Error("连接失败，请检查网络或服务器状态")
        } catch (e: Exception) {
            Result.Error("用户名或密码错误")
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
                val errorMsg = when {
                    response.body()?.message?.contains("用户名") == true -> "用户名已被使用"
                    response.body()?.message?.contains("邮箱") == true -> "邮箱已被注册"
                    else -> response.body()?.message ?: ErrorHandler.getHttpErrorMessage(response, "注册失败")
                }
                Result.Error(errorMsg)
            }
        } catch (e: UnknownHostException) {
            Result.Error("无法连接服务器，请检查网络")
        } catch (e: SocketTimeoutException) {
            Result.Error("连接超时，请检查网络后重试")
        } catch (e: ConnectException) {
            Result.Error("连接失败，请检查网络或服务器状态")
        } catch (e: Exception) {
            Result.Error("注册失败: ${e.message ?: "未知错误"}")
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
