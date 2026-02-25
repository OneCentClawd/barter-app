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
    
    private String type; // "NEW_MESSAGE", "MESSAGE_READ", etc.
    private Long conversationId;
    private MessagePayload message;
    
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
}
