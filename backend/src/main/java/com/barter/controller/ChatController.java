package com.barter.controller;

import com.barter.dto.ApiResponse;
import com.barter.dto.ChatDto;
import com.barter.entity.User;
import com.barter.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/send")
    public ApiResponse<ChatDto.MessageResponse> sendMessage(
            @Valid @RequestBody ChatDto.SendMessageRequest request,
            @AuthenticationPrincipal User user) {
        return ApiResponse.success("消息已发送", chatService.sendMessage(request, user));
    }

    @GetMapping("/conversations")
    public ApiResponse<Page<ChatDto.ConversationResponse>> getConversations(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(chatService.getConversations(user, pageable));
    }

    @GetMapping("/conversations/{id}")
    public ApiResponse<ChatDto.ConversationDetailResponse> getConversationDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 50) Pageable pageable) {
        return ApiResponse.success(chatService.getConversationDetail(id, user, pageable));
    }
}
