package com.barter.app.data.remote

import android.util.Log
import com.barter.app.data.local.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min
import kotlin.math.pow

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
    
    private val _connectionState = MutableSharedFlow<ConnectionState>(replay = 1)
    val connectionState: SharedFlow<ConnectionState> = _connectionState.asSharedFlow()
    
    private val _typingState = MutableSharedFlow<TypingEvent>()
    val typingState: SharedFlow<TypingEvent> = _typingState.asSharedFlow()
    
    // 重连相关
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 10
    private val baseDelayMs = 1000L
    private val maxDelayMs = 30000L
    private var isReconnecting = false
    private var manualDisconnect = false
    
    data class TypingEvent(
        val conversationId: Long,
        val userId: Long,
        val nickname: String?,
        val isTyping: Boolean
    )
    
    enum class ConnectionState {
        CONNECTED, DISCONNECTED, CONNECTING, RECONNECTING, ERROR
    }
    
    fun connect() {
        // 防止重复连接
        if (isConnected() || isReconnecting) {
            Log.d(TAG, "Already connected or reconnecting, skip connect()")
            return
        }
        
        manualDisconnect = false
        
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
                    reconnectAttempts = 0  // 重置重连计数
                    isReconnecting = false
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
                    this@ChatWebSocketManager.webSocket = null
                    scope.launch { 
                        _connectionState.emit(ConnectionState.DISCONNECTED)
                        // 非主动断开时尝试重连
                        if (!manualDisconnect) {
                            scheduleReconnect()
                        }
                    }
                }
                
                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Log.e(TAG, "WebSocket failure", t)
                    this@ChatWebSocketManager.webSocket = null
                    scope.launch { 
                        _connectionState.emit(ConnectionState.ERROR)
                        // 连接失败时尝试重连
                        if (!manualDisconnect) {
                            scheduleReconnect()
                        }
                    }
                }
            })
        }
    }
    
    /**
     * 安排重连（指数退避）
     */
    private fun scheduleReconnect() {
        if (reconnectAttempts >= maxReconnectAttempts) {
            Log.w(TAG, "Max reconnect attempts reached, giving up")
            isReconnecting = false
            return
        }
        
        if (isReconnecting) {
            Log.d(TAG, "Already scheduling reconnect, skip")
            return
        }
        
        isReconnecting = true
        val delayMs = min(baseDelayMs * 2.0.pow(reconnectAttempts.toDouble()).toLong(), maxDelayMs)
        reconnectAttempts++
        
        Log.d(TAG, "Scheduling reconnect in ${delayMs}ms (attempt $reconnectAttempts)")
        
        scope.launch {
            _connectionState.emit(ConnectionState.RECONNECTING)
            delay(delayMs)
            isReconnecting = false
            connect()
        }
    }
    
    /**
     * 确保连接（进入聊天页面时调用）
     */
    fun ensureConnected() {
        if (!isConnected() && !isReconnecting) {
            Log.d(TAG, "Connection not active, connecting...")
            reconnectAttempts = 0  // 重置计数，允许新一轮重连
            connect()
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
        manualDisconnect = true
        reconnectAttempts = 0
        isReconnecting = false
        webSocket?.close(1000, "User disconnected")
        webSocket = null
    }
    
    fun isConnected(): Boolean {
        return webSocket != null
    }
}
