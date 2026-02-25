package com.barter.app.data.remote

import android.util.Log
import com.barter.app.data.local.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

data class WebSocketChatMessage(
    val type: String,
    val conversationId: Long,
    val message: MessagePayload? = null,
    val typing: TypingPayload? = null
)

data class MessagePayload(
    val id: Long,
    val senderId: Long,
    val senderNickname: String?,
    val senderAvatar: String?,
    val content: String,
    val type: String,
    val isRead: Boolean?,
    val createdAt: String?
)

data class TypingPayload(
    val userId: Long,
    val nickname: String?
)

@Singleton
class ChatWebSocketManager @Inject constructor(
    private val tokenManager: TokenManager
) {
    private val TAG = "ChatWebSocket"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .retryOnConnectionFailure(true)
        .build()
    
    private val _incomingMessages = MutableSharedFlow<WebSocketChatMessage>()
    val incomingMessages: SharedFlow<WebSocketChatMessage> = _incomingMessages.asSharedFlow()
    
    private val _connectionState = MutableSharedFlow<ConnectionState>()
    val connectionState: SharedFlow<ConnectionState> = _connectionState.asSharedFlow()
    
    private val _typingState = MutableSharedFlow<TypingEvent>()
    val typingState: SharedFlow<TypingEvent> = _typingState.asSharedFlow()
    
    data class TypingEvent(
        val conversationId: Long,
        val userId: Long,
        val nickname: String?,
        val isTyping: Boolean
    )
    
    enum class ConnectionState {
        CONNECTED, DISCONNECTED, CONNECTING, ERROR
    }
    
    fun connect() {
        scope.launch {
            val token = tokenManager.token.first() ?: return@launch
            
            _connectionState.emit(ConnectionState.CONNECTING)
            
            // 从 BuildConfig 获取 API 地址，替换 http 为 ws
            val httpUrl = com.barter.app.BuildConfig.API_BASE_URL.trimEnd('/')
            val baseUrl = httpUrl.replace("http://", "ws://").replace("https://", "wss://")
            val request = Request.Builder()
                .url("$baseUrl/ws/chat?token=$token")
                .build()
            
            webSocket = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    Log.d(TAG, "WebSocket connected")
                    scope.launch { _connectionState.emit(ConnectionState.CONNECTED) }
                }
                
                override fun onMessage(webSocket: WebSocket, text: String) {
                    Log.d(TAG, "WebSocket message: $text")
                    try {
                        val json = JSONObject(text)
                        val type = json.optString("type")
                        val conversationId = json.optLong("conversationId")
                        
                        when (type) {
                            "NEW_MESSAGE" -> {
                                val messageObj = json.optJSONObject("message")
                                if (messageObj != null) {
                                    val message = WebSocketChatMessage(
                                        type = type,
                                        conversationId = conversationId,
                                        message = MessagePayload(
                                            id = messageObj.optLong("id"),
                                            senderId = messageObj.optLong("senderId"),
                                            senderNickname = messageObj.optString("senderNickname"),
                                            senderAvatar = messageObj.optString("senderAvatar").takeIf { it.isNotEmpty() },
                                            content = messageObj.optString("content"),
                                            type = messageObj.optString("type"),
                                            isRead = messageObj.optBoolean("isRead"),
                                            createdAt = messageObj.optString("createdAt")
                                        )
                                    )
                                    scope.launch { _incomingMessages.emit(message) }
                                }
                            }
                            "TYPING" -> {
                                val typingObj = json.optJSONObject("typing")
                                if (typingObj != null) {
                                    scope.launch {
                                        _typingState.emit(TypingEvent(
                                            conversationId = conversationId,
                                            userId = typingObj.optLong("userId"),
                                            nickname = typingObj.optString("nickname"),
                                            isTyping = true
                                        ))
                                    }
                                }
                            }
                            "STOP_TYPING" -> {
                                val typingObj = json.optJSONObject("typing")
                                if (typingObj != null) {
                                    scope.launch {
                                        _typingState.emit(TypingEvent(
                                            conversationId = conversationId,
                                            userId = typingObj.optLong("userId"),
                                            nickname = typingObj.optString("nickname"),
                                            isTyping = false
                                        ))
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse WebSocket message", e)
                    }
                }
                
                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    Log.d(TAG, "WebSocket closing: $code $reason")
                }
                
                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    Log.d(TAG, "WebSocket closed: $code $reason")
                    scope.launch { _connectionState.emit(ConnectionState.DISCONNECTED) }
                }
                
                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Log.e(TAG, "WebSocket failure", t)
                    scope.launch { _connectionState.emit(ConnectionState.ERROR) }
                }
            })
        }
    }
    
    /**
     * 发送正在输入状态
     */
    fun sendTyping(targetUserId: Long, conversationId: Long, nickname: String?) {
        val json = JSONObject().apply {
            put("type", "TYPING")
            put("targetUserId", targetUserId)
            put("conversationId", conversationId)
            put("nickname", nickname ?: "")
        }
        webSocket?.send(json.toString())
    }
    
    /**
     * 发送停止输入状态
     */
    fun sendStopTyping(targetUserId: Long, conversationId: Long) {
        val json = JSONObject().apply {
            put("type", "STOP_TYPING")
            put("targetUserId", targetUserId)
            put("conversationId", conversationId)
        }
        webSocket?.send(json.toString())
    }
    
    fun disconnect() {
        webSocket?.close(1000, "User disconnected")
        webSocket = null
    }
    
    fun isConnected(): Boolean {
        return webSocket != null
    }
}
