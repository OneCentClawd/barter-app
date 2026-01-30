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
class TradeRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun createTrade(
        targetItemId: Long,
        offeredItemId: Long,
        message: String?
    ): Result<TradeRequest> {
        return try {
            val response = apiService.createTrade(
                CreateTradeRequest(targetItemId, offeredItemId, message)
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                Result.Error(response.body()?.message ?: ErrorHandler.getHttpErrorMessage(response, "发起交换失败"))
            }
        } catch (e: UnknownHostException) {
            Result.Error("无法连接服务器，请检查网络")
        } catch (e: SocketTimeoutException) {
            Result.Error("连接超时，请检查网络后重试")
        } catch (e: ConnectException) {
            Result.Error("连接失败，请检查网络或服务器状态")
        } catch (e: Exception) {
            Result.Error("发起交换失败: ${e.message ?: "未知错误"}")
        }
    }

    suspend fun getTrade(tradeId: Long): Result<TradeRequest> {
        return try {
            val response = apiService.getTrade(tradeId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                Result.Error(response.body()?.message ?: ErrorHandler.getHttpErrorMessage(response, "获取交换详情失败"))
            }
        } catch (e: UnknownHostException) {
            Result.Error("无法连接服务器，请检查网络")
        } catch (e: SocketTimeoutException) {
            Result.Error("连接超时，请检查网络后重试")
        } catch (e: ConnectException) {
            Result.Error("连接失败，请检查网络或服务器状态")
        } catch (e: Exception) {
            Result.Error("获取交换详情失败: ${e.message ?: "未知错误"}")
        }
    }

    suspend fun updateTradeStatus(tradeId: Long, status: TradeStatus): Result<TradeRequest> {
        return try {
            val response = apiService.updateTradeStatus(tradeId, UpdateTradeStatusRequest(status))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                Result.Error(response.body()?.message ?: ErrorHandler.getHttpErrorMessage(response, "更新交换状态失败"))
            }
        } catch (e: UnknownHostException) {
            Result.Error("无法连接服务器，请检查网络")
        } catch (e: SocketTimeoutException) {
            Result.Error("连接超时，请检查网络后重试")
        } catch (e: ConnectException) {
            Result.Error("连接失败，请检查网络或服务器状态")
        } catch (e: Exception) {
            Result.Error("更新交换状态失败: ${e.message ?: "未知错误"}")
        }
    }

    suspend fun getSentTrades(page: Int = 0, size: Int = 20): Result<PageResponse<TradeRequest>> {
        return try {
            val response = apiService.getSentTrades(page, size)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                Result.Error(response.body()?.message ?: ErrorHandler.getHttpErrorMessage(response, "获取发送的交换请求失败"))
            }
        } catch (e: UnknownHostException) {
            Result.Error("无法连接服务器，请检查网络")
        } catch (e: SocketTimeoutException) {
            Result.Error("连接超时，请检查网络后重试")
        } catch (e: ConnectException) {
            Result.Error("连接失败，请检查网络或服务器状态")
        } catch (e: Exception) {
            Result.Error("获取发送的交换请求失败: ${e.message ?: "未知错误"}")
        }
    }

    suspend fun getReceivedTrades(page: Int = 0, size: Int = 20): Result<PageResponse<TradeRequest>> {
        return try {
            val response = apiService.getReceivedTrades(page, size)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()!!.data!!)
            } else {
                Result.Error(response.body()?.message ?: ErrorHandler.getHttpErrorMessage(response, "获取收到的交换请求失败"))
            }
        } catch (e: UnknownHostException) {
            Result.Error("无法连接服务器，请检查网络")
        } catch (e: SocketTimeoutException) {
            Result.Error("连接超时，请检查网络后重试")
        } catch (e: ConnectException) {
            Result.Error("连接失败，请检查网络或服务器状态")
        } catch (e: Exception) {
            Result.Error("获取收到的交换请求失败: ${e.message ?: "未知错误"}")
        }
    }
}
