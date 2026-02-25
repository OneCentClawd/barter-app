package com.barter.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {
    
    // 消息类型: NEW_MESSAGE, MESSAGE_READ, TYPING, STOP_TYPING
    private String type;
    private Long conversationId;
    private MessagePayload message;
    private TypingPayload typing;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessagePayload {
        private Long id;
        private Long senderId;
        private String senderNickname;
        private String senderAvatar;
        private String content;
        private String type;
        private Boolean isRead;
        private String createdAt;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TypingPayload {
        private Long userId;
        private String nickname;
    }
}
