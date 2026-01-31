package com.barter.app.data.repository

import android.content.Context
import android.net.Uri
import com.barter.app.data.local.TokenManager
import com.barter.app.data.model.*
import com.barter.app.data.remote.ApiService
import com.barter.app.util.ErrorHandler
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context
) {
    suspend fun getMyProfile(): Result<User> {
        return try {
            val response = apiService.getMyProfile()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "登录已过期，请重新登录"
                    else -> response.body()?.message ?: ErrorHandler.getHttpErrorMessage(response, "获取个人资料失败")
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
            Result.Error("获取个人资料失败: ${e.message ?: "未知错误"}")
        }
    }

    suspend fun getProfile(userId: Long): Result<User> {
        return try {
            val response = apiService.getProfile(userId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                val errorMsg = when (response.code()) {
                    404 -> "用户不存在"
                    else -> response.body()?.message ?: ErrorHandler.getHttpErrorMessage(response, "获取用户资料失败")
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
            Result.Error("获取用户资料失败: ${e.message ?: "未知错误"}")
        }
    }

    suspend fun updateProfile(nickname: String?, phone: String?, bio: String?): Result<User> {
        return try {
            val response = apiService.updateProfile(UpdateProfileRequest(nickname, phone, bio))
            if (response.isSuccessful && response.body()?.success == true) {
                nickname?.let { tokenManager.updateNickname(it) }
                Result.Success(response.body()!!.data!!)
            } else {
                Result.Error(response.body()?.message ?: ErrorHandler.getHttpErrorMessage(response, "更新资料失败"))
            }
        } catch (e: UnknownHostException) {
            Result.Error("无法连接服务器，请检查网络")
        } catch (e: SocketTimeoutException) {
            Result.Error("连接超时，请检查网络后重试")
        } catch (e: ConnectException) {
            Result.Error("连接失败，请检查网络或服务器状态")
        } catch (e: Exception) {
            Result.Error("更新资料失败: ${e.message ?: "未知错误"}")
        }
    }

    suspend fun updateAvatar(uri: Uri): Result<User> {
        return try {
            val part = uriToMultipartBodyPart(uri, "avatar") ?: return Result.Error("图片处理失败，请选择其他图片")
            val response = apiService.updateAvatar(part)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()!!.data!!.avatar?.let { tokenManager.updateAvatar(it) }
                Result.Success(response.body()!!.data!!)
            } else {
                val errorMsg = when (response.code()) {
                    413 -> "图片太大，请压缩后重试"
                    else -> response.body()?.message ?: ErrorHandler.getHttpErrorMessage(response, "更新头像失败")
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
            Result.Error("更新头像失败: ${e.message ?: "未知错误"}")
        }
    }

    private fun uriToMultipartBodyPart(uri: Uri, partName: String): MultipartBody.Part? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val file = File(context.cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()

            val requestBody = file.asRequestBody("image/*".toMediaType())
            MultipartBody.Part.createFormData(partName, file.name, requestBody)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit> {
        return try {
            val response = apiService.changePassword(ChangePasswordRequest(oldPassword, newPassword))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(Unit)
            } else {
                val errorMsg = when (response.code()) {
                    400 -> response.body()?.message ?: "当前密码错误"
                    else -> response.body()?.message ?: ErrorHandler.getHttpErrorMessage(response, "修改密码失败")
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
            Result.Error("修改密码失败: ${e.message ?: "未知错误"}")
        }
    }

    suspend fun getSettings(): Result<UserSettings> {
        return try {
            val response = apiService.getSettings()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                Result.Error(response.body()?.message ?: ErrorHandler.getHttpErrorMessage(response, "获取设置失败"))
            }
        } catch (e: UnknownHostException) {
            Result.Error("无法连接服务器，请检查网络")
        } catch (e: SocketTimeoutException) {
            Result.Error("连接超时，请检查网络后重试")
        } catch (e: ConnectException) {
            Result.Error("连接失败，请检查网络或服务器状态")
        } catch (e: Exception) {
            Result.Error("获取设置失败: ${e.message ?: "未知错误"}")
        }
    }

    suspend fun updateSettings(request: UpdateSettingsRequest): Result<UserSettings> {
        return try {
            val response = apiService.updateSettings(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                Result.Error(response.body()?.message ?: ErrorHandler.getHttpErrorMessage(response, "更新设置失败"))
            }
        } catch (e: UnknownHostException) {
            Result.Error("无法连接服务器，请检查网络")
        } catch (e: SocketTimeoutException) {
            Result.Error("连接超时，请检查网络后重试")
        } catch (e: ConnectException) {
            Result.Error("连接失败，请检查网络或服务器状态")
        } catch (e: Exception) {
            Result.Error("更新设置失败: ${e.message ?: "未知错误"}")
        }
    }

    suspend fun getLoginRecords(): Result<List<LoginRecord>> {
        return try {
            val response = apiService.getLoginRecords()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data ?: emptyList())
            } else {
                Result.Error(response.body()?.message ?: ErrorHandler.getHttpErrorMessage(response, "获取登录记录失败"))
            }
        } catch (e: UnknownHostException) {
            Result.Error("无法连接服务器，请检查网络")
        } catch (e: SocketTimeoutException) {
            Result.Error("连接超时，请检查网络后重试")
        } catch (e: ConnectException) {
            Result.Error("连接失败，请检查网络或服务器状态")
        } catch (e: Exception) {
            Result.Error("获取登录记录失败: ${e.message ?: "未知错误"}")
        }
    }
}
