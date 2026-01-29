package com.barter.app.data.repository

import com.barter.app.data.model.*
import com.barter.app.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun sendMessage(receiverId: Long, content: String): Result<Message> {
        return try {
            val response = apiService.sendMessage(SendMessageRequest(receiverId, content))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                Result.Error(response.body()?.message ?: "发送消息失败")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "网络错误")
        }
    }

    suspend fun getConversations(page: Int = 0, size: Int = 20): Result<PageResponse<Conversation>> {
        return try {
            val response = apiService.getConversations(page, size)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                Result.Error(response.body()?.message ?: "获取对话列表失败")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "网络错误")
        }
    }

    suspend fun getConversationDetail(
        conversationId: Long,
        page: Int = 0,
        size: Int = 50
    ): Result<ConversationDetail> {
        return try {
            val response = apiService.getConversationDetail(conversationId, page, size)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                Result.Error(response.body()?.message ?: "获取对话详情失败")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "网络错误")
        }
    }
}
