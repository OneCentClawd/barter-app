package com.barter.app.data.repository

import com.barter.app.data.model.*
import com.barter.app.data.remote.ApiService
import com.barter.app.util.ErrorHandler
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
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
                Result.Error(response.body()?.message ?: ErrorHandler.getHttpErrorMessage(response, "发送消息失败"))
            }
        } catch (e: UnknownHostException) {
            Result.Error("无法连接服务器，请检查网络")
        } catch (e: SocketTimeoutException) {
            Result.Error("连接超时，消息可能未发送")
        } catch (e: ConnectException) {
            Result.Error("连接失败，请检查网络或服务器状态")
        } catch (e: Exception) {
            Result.Error("发送消息失败: ${e.message ?: "未知错误"}")
        }
    }

    suspend fun getConversations(page: Int = 0, size: Int = 20): Result<PageResponse<Conversation>> {
        return try {
            val response = apiService.getConversations(page, size)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                Result.Error(response.body()?.message ?: ErrorHandler.getHttpErrorMessage(response, "获取对话列表失败"))
            }
        } catch (e: UnknownHostException) {
            Result.Error("无法连接服务器，请检查网络")
        } catch (e: SocketTimeoutException) {
            Result.Error("连接超时，请检查网络后重试")
        } catch (e: ConnectException) {
            Result.Error("连接失败，请检查网络或服务器状态")
        } catch (e: Exception) {
            Result.Error("获取对话列表失败: ${e.message ?: "未知错误"}")
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
                Result.Error(response.body()?.message ?: ErrorHandler.getHttpErrorMessage(response, "获取对话详情失败"))
            }
        } catch (e: UnknownHostException) {
            Result.Error("无法连接服务器，请检查网络")
        } catch (e: SocketTimeoutException) {
            Result.Error("连接超时，请检查网络后重试")
        } catch (e: ConnectException) {
            Result.Error("连接失败，请检查网络或服务器状态")
        } catch (e: Exception) {
            Result.Error("获取对话详情失败: ${e.message ?: "未知错误"}")
        }
    }
}
