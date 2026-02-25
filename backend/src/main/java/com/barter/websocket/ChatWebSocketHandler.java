package com.barter.websocket;

import com.barter.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // userId -> WebSocket session
    private static final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = extractUserIdFromSession(session);
        if (userId != null) {
            userSessions.put(userId, session);
            log.info("WebSocket connected: userId={}", userId);
        } else {
            log.warn("WebSocket connection rejected: no valid token");
            session.close(CloseStatus.NOT_ACCEPTABLE);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = extractUserIdFromSession(session);
        if (userId != null) {
            userSessions.remove(userId);
            log.info("WebSocket disconnected: userId={}", userId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 处理客户端发送的消息（如 typing 状态）
        try {
            String payload = message.getPayload();
            log.debug("Received message: {}", payload);
            
            var jsonNode = objectMapper.readTree(payload);
            String type = jsonNode.has("type") ? jsonNode.get("type").asText() : null;
            
            if ("TYPING".equals(type) || "STOP_TYPING".equals(type)) {
                Long targetUserId = jsonNode.has("targetUserId") ? jsonNode.get("targetUserId").asLong() : null;
                Long conversationId = jsonNode.has("conversationId") ? jsonNode.get("conversationId").asLong() : null;
                Long senderId = extractUserIdFromSession(session);
                String senderNickname = jsonNode.has("nickname") ? jsonNode.get("nickname").asText() : null;
                
                if (targetUserId != null && senderId != null) {
                    // 转发 typing 状态给对方
                    WebSocketMessage wsMessage = WebSocketMessage.builder()
                            .type(type)
                            .conversationId(conversationId)
                            .typing(WebSocketMessage.TypingPayload.builder()
                                    .userId(senderId)
                                    .nickname(senderNickname)
                                    .build())
                            .build();
                    sendMessageToUser(targetUserId, wsMessage);
                }
            }
        } catch (Exception e) {
            log.error("Failed to process WebSocket message", e);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error", exception);
    }

    /**
     * 向指定用户推送新消息
     */
    public void sendMessageToUser(Long userId, Object messageData) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String json = objectMapper.writeValueAsString(messageData);
                session.sendMessage(new TextMessage(json));
                log.debug("Sent message to userId={}: {}", userId, json);
            } catch (IOException e) {
                log.error("Failed to send WebSocket message to userId={}", userId, e);
            }
        }
    }

    /**
     * 从 WebSocket 连接中提取用户 ID
     * 客户端连接时需要在 URL 参数中带上 token：ws://host/ws/chat?token=xxx
     */
    private Long extractUserIdFromSession(WebSocketSession session) {
        try {
            URI uri = session.getUri();
            if (uri != null) {
                String query = uri.getQuery();
                if (query != null && query.contains("token=")) {
                    String token = query.split("token=")[1].split("&")[0];
                    if (jwtTokenProvider.validateToken(token)) {
                        return jwtTokenProvider.getUserIdFromToken(token);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to extract userId from WebSocket session", e);
        }
        return null;
    }
    
    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(Long userId) {
        WebSocketSession session = userSessions.get(userId);
        return session != null && session.isOpen();
    }
}
