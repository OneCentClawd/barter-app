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
    val message: MessagePayload
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
    
    enum class ConnectionState {
        CONNECTED, DISCONNECTED, CONNECTING, ERROR
    }
    
    fun connect() {
        scope.launch {
            val token = tokenManager.token.first() ?: return@launch
            
            _connectionState.emit(ConnectionState.CONNECTING)
            
            // TODO: 替换成实际的服务器地址
            val baseUrl = "ws://150.109.72.152:9527"
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
    
    fun disconnect() {
        webSocket?.close(1000, "User disconnected")
        webSocket = null
    }
    
    fun isConnected(): Boolean {
        return webSocket != null
    }
}
