package com.barter.dto;

import com.barter.entity.Message;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

public class ChatDto {

    @Data
    public static class SendMessageRequest {
        @NotNull(message = "对方用户ID不能为空")
        private Long receiverId;

        @NotBlank(message = "消息内容不能为空")
        private String content;

        private Message.MessageType type = Message.MessageType.TEXT;
    }

    @Data
    public static class MessageResponse {
        private Long id;
        private Long senderId;
        private String senderNickname;
        private String senderAvatar;
        private String content;
        private Message.MessageType type;
        private Boolean isRead;
        private LocalDateTime createdAt;
    }

    @Data
    public static class ConversationResponse {
        private Long id;
        private ItemDto.UserBrief otherUser;
        private MessageResponse lastMessage;
        private Integer unreadCount;
        private LocalDateTime lastMessageAt;
    }

    @Data
    public static class ConversationDetailResponse {
        private Long id;
        private ItemDto.UserBrief otherUser;
        private List<MessageResponse> messages;
    }
}
