package com.barter.app.data.repository

import android.content.Context
import android.net.Uri
import com.barter.app.data.model.*
import com.barter.app.data.remote.ApiService
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemRepository @Inject constructor(
    private val apiService: ApiService,
    @ApplicationContext private val context: Context
) {
    suspend fun getItems(page: Int = 0, size: Int = 20): Result<PageResponse<ItemListItem>> {
        return try {
            val response = apiService.getItems(page, size)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                Result.Error(response.body()?.message ?: "获取物品列表失败")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "网络错误")
        }
    }

    suspend fun searchItems(keyword: String, page: Int = 0, size: Int = 20): Result<PageResponse<ItemListItem>> {
        return try {
            val response = apiService.searchItems(keyword, page, size)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                Result.Error(response.body()?.message ?: "搜索失败")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "网络错误")
        }
    }

    suspend fun getItem(itemId: Long): Result<Item> {
        return try {
            val response = apiService.getItem(itemId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                Result.Error(response.body()?.message ?: "获取物品详情失败")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "网络错误")
        }
    }

    suspend fun getMyItems(page: Int = 0, size: Int = 20): Result<PageResponse<ItemListItem>> {
        return try {
            val response = apiService.getMyItems(page, size)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                Result.Error(response.body()?.message ?: "获取我的物品失败")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "网络错误")
        }
    }

    suspend fun getMyAvailableItems(): Result<List<ItemListItem>> {
        return try {
            val response = apiService.getMyAvailableItems()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                Result.Error(response.body()?.message ?: "获取可用物品失败")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "网络错误")
        }
    }

    suspend fun createItem(
        title: String,
        description: String?,
        category: String?,
        condition: ItemCondition,
        wantedItems: String?,
        imageUris: List<Uri>
    ): Result<Item> {
        return try {
            val itemRequest = CreateItemRequest(title, description, category, condition, wantedItems)
            val json = Gson().toJson(itemRequest)
            val itemBody = json.toRequestBody("application/json".toMediaType())

            val imageParts = imageUris.mapNotNull { uri ->
                uriToMultipartBodyPart(uri, "images")
            }

            val response = apiService.createItem(itemBody, imageParts.ifEmpty { null })
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "登录已过期，请重新登录"
                    403 -> "没有权限执行此操作"
                    413 -> "图片太大，请压缩后重试"
                    500 -> "服务器错误，请稍后重试"
                    else -> response.body()?.message ?: "发布物品失败 (${response.code()})"
                }
                Result.Error(errorMsg)
            }
        } catch (e: java.net.UnknownHostException) {
            Result.Error("无法连接服务器，请检查网络")
        } catch (e: java.net.SocketTimeoutException) {
            Result.Error("连接超时，请检查网络后重试")
        } catch (e: java.net.ConnectException) {
            Result.Error("连接失败，服务器可能未启动")
        } catch (e: Exception) {
            Result.Error("发布失败: ${e.message ?: "未知错误"}")
        }
    }

    suspend fun updateItem(itemId: Long, request: UpdateItemRequest): Result<Item> {
        return try {
            val response = apiService.updateItem(itemId, request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                Result.Error(response.body()?.message ?: "更新物品失败")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "网络错误")
        }
    }

    suspend fun deleteItem(itemId: Long): Result<Unit> {
        return try {
            val response = apiService.deleteItem(itemId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(Unit)
            } else {
                Result.Error(response.body()?.message ?: "删除物品失败")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "网络错误")
        }
    }

    private fun uriToMultipartBodyPart(uri: Uri, partName: String): MultipartBody.Part? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
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
