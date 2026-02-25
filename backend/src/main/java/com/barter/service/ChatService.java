package com.barter.service;

import com.barter.dto.ChatDto;
import com.barter.entity.Conversation;
import com.barter.entity.Message;
import com.barter.entity.User;
import com.barter.repository.ConversationRepository;
import com.barter.repository.MessageRepository;
import com.barter.repository.UserRepository;
import com.barter.websocket.ChatWebSocketHandler;
import com.barter.websocket.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ItemService itemService;
    private final SystemConfigService systemConfigService;
    private final AiService aiService;
    private final AiReplyService aiReplyService;
    private final ChatWebSocketHandler webSocketHandler;

    @Transactional
    public ChatDto.MessageResponse sendMessage(ChatDto.SendMessageRequest request, User sender) {
        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (sender.getId().equals(receiver.getId())) {
            throw new RuntimeException("不能给自己发消息");
        }

        // 检查聊天权限
        boolean senderIsAdmin = sender.getIsAdmin() != null && sender.getIsAdmin();
        boolean receiverIsAdmin = receiver.getIsAdmin() != null && receiver.getIsAdmin();
        boolean allowUserChat = systemConfigService.isAllowUserChat();
        boolean isAiChat = aiService.isAiUser(receiver.getId()) || aiService.isAiUser(sender.getId());
        
        // 如果不允许用户间聊天，且双方都不是管理员，且不是AI聊天，则拒绝
        if (!allowUserChat && !senderIsAdmin && !receiverIsAdmin && !isAiChat) {
            throw new RuntimeException("目前只能与客服人员聊天");
        }

        // 查找或创建对话
        Conversation conversation = conversationRepository.findByUsers(sender, receiver)
                .orElseGet(() -> {
                    Conversation newConv = new Conversation();
                    newConv.setUser1(sender);
                    newConv.setUser2(receiver);
                    return conversationRepository.save(newConv);
                });

        // 创建消息
        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(request.getContent());
        message.setType(request.getType());

        message = messageRepository.save(message);

        // 更新对话的最后消息时间
        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        // 通过 WebSocket 推送给接收者
        pushMessageToUser(receiver.getId(), conversation.getId(), message);

        // 如果收件人是AI用户，异步生成AI回复
        if (aiService.isAiUser(receiver.getId())) {
            aiReplyService.generateAndSendReply(
                    conversation.getId(),
                    receiver.getId(),
                    sender.getId(),
                    receiver.getNickname(),
                    receiver.getAvatar(),
                    request.getContent()
            );
        }

        return toMessageResponse(message);
    }

    /**
     * 通过 WebSocket 推送消息给用户
     */
    private void pushMessageToUser(Long userId, Long conversationId, Message message) {
        WebSocketMessage wsMessage = WebSocketMessage.builder()
                .type("NEW_MESSAGE")
                .conversationId(conversationId)
                .message(WebSocketMessage.MessagePayload.builder()
                        .id(message.getId())
                        .senderId(message.getSender().getId())
                        .senderNickname(message.getSender().getNickname())
                        .senderAvatar(message.getSender().getAvatar())
                        .content(message.getContent())
                        .type(message.getType().name())
                        .isRead(message.getIsRead())
                        .createdAt(message.getCreatedAt() != null ? 
                                message.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)
                        .build())
                .build();
        
        webSocketHandler.sendMessageToUser(userId, wsMessage);
    }

    public Page<ChatDto.ConversationResponse> getConversations(User user, Pageable pageable) {
        return conversationRepository.findByUser(user, pageable)
                .map(conv -> toConversationResponse(conv, user));
    }

    public ChatDto.ConversationDetailResponse getConversationDetail(Long conversationId, User user, Pageable pageable) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("对话不存在"));

        // 验证权限
        if (!conversation.getUser1().getId().equals(user.getId()) &&
            !conversation.getUser2().getId().equals(user.getId())) {
            throw new RuntimeException("无权查看此对话");
        }

        User otherUser = conversation.getUser1().getId().equals(user.getId()) ?
                conversation.getUser2() : conversation.getUser1();

        ChatDto.ConversationDetailResponse response = new ChatDto.ConversationDetailResponse();
        response.setId(conversation.getId());
        response.setOtherUser(itemService.toUserBrief(otherUser));
        response.setMessages(messageRepository.findByConversationOrderByCreatedAtDesc(conversation, pageable)
                .getContent().stream()
                .map(this::toMessageResponse)
                .collect(Collectors.toList()));

        return response;
    }

    private ChatDto.ConversationResponse toConversationResponse(Conversation conversation, User currentUser) {
        User otherUser = conversation.getUser1().getId().equals(currentUser.getId()) ?
                conversation.getUser2() : conversation.getUser1();

        ChatDto.ConversationResponse response = new ChatDto.ConversationResponse();
        response.setId(conversation.getId());
        response.setOtherUser(itemService.toUserBrief(otherUser));
        response.setLastMessageAt(conversation.getLastMessageAt());

        // 获取最后一条消息
        Page<Message> lastMessages = messageRepository.findByConversationOrderByCreatedAtDesc(
                conversation, Pageable.ofSize(1));
        if (!lastMessages.isEmpty()) {
            response.setLastMessage(toMessageResponse(lastMessages.getContent().get(0)));
        }

        return response;
    }

    private ChatDto.MessageResponse toMessageResponse(Message message) {
        ChatDto.MessageResponse response = new ChatDto.MessageResponse();
        response.setId(message.getId());
        response.setSenderId(message.getSender().getId());
        response.setSenderNickname(message.getSender().getNickname());
        response.setSenderAvatar(message.getSender().getAvatar());
        response.setContent(message.getContent());
        response.setType(message.getType());
        response.setIsRead(message.getIsRead());
        response.setCreatedAt(message.getCreatedAt());
        return response;
    }
}
