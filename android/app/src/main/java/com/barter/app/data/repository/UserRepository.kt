package com.barter.app.data.repository

import android.content.Context
import android.net.Uri
import com.barter.app.data.local.TokenManager
import com.barter.app.data.model.*
import com.barter.app.data.remote.ApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
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
                Result.Error(response.body()?.message ?: "获取个人资料失败")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "网络错误")
        }
    }

    suspend fun getProfile(userId: Long): Result<User> {
        return try {
            val response = apiService.getProfile(userId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                Result.Error(response.body()?.message ?: "获取用户资料失败")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "网络错误")
        }
    }

    suspend fun updateProfile(nickname: String?, phone: String?, bio: String?): Result<User> {
        return try {
            val response = apiService.updateProfile(UpdateProfileRequest(nickname, phone, bio))
            if (response.isSuccessful && response.body()?.success == true) {
                nickname?.let { tokenManager.updateNickname(it) }
                Result.Success(response.body()!!.data!!)
            } else {
                Result.Error(response.body()?.message ?: "更新资料失败")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "网络错误")
        }
    }

    suspend fun updateAvatar(uri: Uri): Result<User> {
        return try {
            val part = uriToMultipartBodyPart(uri, "avatar") ?: return Result.Error("图片处理失败")
            val response = apiService.updateAvatar(part)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()!!.data!!.avatar?.let { tokenManager.updateAvatar(it) }
                Result.Success(response.body()!!.data!!)
            } else {
                Result.Error(response.body()?.message ?: "更新头像失败")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "网络错误")
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
}
