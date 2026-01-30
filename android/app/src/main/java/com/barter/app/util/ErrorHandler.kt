package com.barter.app.util

import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ErrorHandler {
    
    fun <T> getHttpErrorMessage(response: Response<T>, defaultMsg: String): String {
        return when (response.code()) {
            400 -> "请求参数错误"
            401 -> "登录已过期，请重新登录"
            403 -> "没有权限执行此操作"
            404 -> "请求的资源不存在"
            413 -> "上传的文件太大"
            422 -> "数据验证失败"
            429 -> "请求太频繁，请稍后重试"
            500 -> "服务器内部错误，请稍后重试"
            502 -> "服务器暂时不可用"
            503 -> "服务器正在维护中"
            else -> "$defaultMsg (错误码: ${response.code()})"
        }
    }
    
    fun getExceptionMessage(e: Exception, defaultMsg: String): String {
        return when (e) {
            is UnknownHostException -> "无法连接服务器，请检查网络"
            is SocketTimeoutException -> "连接超时，请检查网络后重试"
            is ConnectException -> "连接失败，请检查网络或服务器状态"
            is javax.net.ssl.SSLException -> "安全连接失败，请检查网络"
            else -> "$defaultMsg: ${e.message ?: "未知错误"}"
        }
    }
}
