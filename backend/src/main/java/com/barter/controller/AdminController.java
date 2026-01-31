package com.barter.controller;

import com.barter.dto.ApiResponse;
import com.barter.entity.User;
import com.barter.service.SystemConfigService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final SystemConfigService systemConfigService;

    // 获取系统配置
    @GetMapping("/config")
    public ApiResponse<SystemConfigResponse> getConfig(@AuthenticationPrincipal User user) {
        checkAdmin(user);
        
        SystemConfigResponse response = new SystemConfigResponse();
        response.setAllowUserChat(systemConfigService.isAllowUserChat());
        response.setAllowUserViewItems(systemConfigService.isAllowUserViewItems());
        return ApiResponse.success(response);
    }

    // 设置是否允许用户间聊天
    @PostMapping("/config/allow-user-chat")
    public ApiResponse<Void> setAllowUserChat(
            @RequestBody AllowRequest request,
            @AuthenticationPrincipal User user) {
        checkAdmin(user);
        
        systemConfigService.setAllowUserChat(request.isAllow());
        return ApiResponse.success(request.isAllow() ? "已开启用户间聊天" : "已关闭用户间聊天", null);
    }

    // 设置是否允许用户看到其他用户的物品
    @PostMapping("/config/allow-user-view-items")
    public ApiResponse<Void> setAllowUserViewItems(
            @RequestBody AllowRequest request,
            @AuthenticationPrincipal User user) {
        checkAdmin(user);
        
        systemConfigService.setAllowUserViewItems(request.isAllow());
        return ApiResponse.success(request.isAllow() ? "已开启用户物品可见" : "已关闭用户物品可见", null);
    }

    private void checkAdmin(User user) {
        if (user.getIsAdmin() == null || !user.getIsAdmin()) {
            throw new RuntimeException("需要管理员权限");
        }
    }

    @Data
    public static class SystemConfigResponse {
        private boolean allowUserChat;
        private boolean allowUserViewItems;
    }

    @Data
    public static class AllowRequest {
        private boolean allow;
    }
}
