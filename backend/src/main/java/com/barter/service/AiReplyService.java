package com.barter.service;

import com.barter.entity.Conversation;
import com.barter.entity.Message;
import com.barter.entity.User;
import com.barter.repository.ConversationRepository;
import com.barter.repository.MessageRepository;
import com.barter.websocket.ChatWebSocketHandler;
import com.barter.websocket.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiReplyService {

    private final AiService aiService;
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ChatWebSocketHandler webSocketHandler;

    /**
     * 异步生成 AI 回复并推送给用户
     */
    @Async
    @Transactional
    public void generateAndSendReply(Long conversationId, Long aiUserId, Long humanUserId, 
                                      String aiNickname, String aiAvatar, String userMessage) {
        try {
            log.info("开始生成 AI 回复, conversationId={}, humanUserId={}", conversationId, humanUserId);
            
            // 先发送"正在输入"状态
            WebSocketMessage typingMessage = WebSocketMessage.builder()
                    .type("TYPING")
                    .conversationId(conversationId)
                    .typing(WebSocketMessage.TypingPayload.builder()
                            .userId(aiUserId)
                            .nickname(aiNickname)
                            .build())
                    .build();
            webSocketHandler.sendMessageToUser(humanUserId, typingMessage);
            
            // 获取 AI 回复
            String aiReply = aiService.getAiResponse(userMessage, humanUserId);
            
            // 获取对话和 AI 用户
            Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
            if (conversation == null) {
                log.error("对话不存在: {}", conversationId);
                return;
            }
            
            // 创建 AI 回复消息
            Message aiMessage = new Message();
            aiMessage.setConversation(conversation);
            // 设置发送者 - 通过 ID 创建一个引用
            User aiUser = new User();
            aiUser.setId(aiUserId);
            aiUser.setNickname(aiNickname);
            aiUser.setAvatar(aiAvatar);
            aiMessage.setSender(aiUser);
            aiMessage.setContent(aiReply);
            aiMessage.setType(Message.MessageType.TEXT);
            aiMessage.setIsRead(false);
            
            aiMessage = messageRepository.save(aiMessage);
            
            // 更新对话的最后消息时间
            conversation.setLastMessageAt(LocalDateTime.now());
            conversationRepository.save(conversation);
            
            // 通过 WebSocket 推送 AI 回复给用户（会自动清除 typing 状态）
            WebSocketMessage wsMessage = WebSocketMessage.builder()
                    .type("NEW_MESSAGE")
                    .conversationId(conversationId)
                    .message(WebSocketMessage.MessagePayload.builder()
                            .id(aiMessage.getId())
                            .senderId(aiUserId)
                            .senderNickname(aiNickname)
                            .senderAvatar(aiAvatar)
                            .content(aiReply)
                            .type("TEXT")
                            .isRead(false)
                            .createdAt(aiMessage.getCreatedAt() != null ?
                                    aiMessage.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)
                            .build())
                    .build();
            
            webSocketHandler.sendMessageToUser(humanUserId, wsMessage);
            
            log.info("AI 回复已发送, messageId={}", aiMessage.getId());
            
        } catch (Exception e) {
            log.error("AI 回复生成失败", e);
            // 发送停止输入状态
            WebSocketMessage stopTyping = WebSocketMessage.builder()
                    .type("STOP_TYPING")
                    .conversationId(conversationId)
                    .typing(WebSocketMessage.TypingPayload.builder()
                            .userId(aiUserId)
                            .nickname(aiNickname)
                            .build())
                    .build();
            webSocketHandler.sendMessageToUser(humanUserId, stopTyping);
        }
    }
}
