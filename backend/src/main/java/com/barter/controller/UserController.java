package com.barter.controller;

import com.barter.dto.ApiResponse;
import com.barter.dto.UserDto;
import com.barter.entity.User;
import com.barter.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserDto.ProfileResponse> getMyProfile(@AuthenticationPrincipal User user) {
        return ApiResponse.success(userService.getMyProfile(user));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserDto.ProfileResponse> getProfile(@PathVariable Long id) {
        return ApiResponse.success(userService.getProfile(id));
    }

    @PutMapping("/me")
    public ApiResponse<UserDto.ProfileResponse> updateProfile(
            @Valid @RequestBody UserDto.UpdateProfileRequest request,
            @AuthenticationPrincipal User user) {
        return ApiResponse.success("资料更新成功", userService.updateProfile(request, user));
    }

    @PostMapping("/me/avatar")
    public ApiResponse<UserDto.ProfileResponse> updateAvatar(
            @RequestParam("avatar") MultipartFile file,
            @AuthenticationPrincipal User user) {
        return ApiResponse.success("头像更新成功", userService.updateAvatar(file, user));
    }

    @PostMapping("/me/password")
    public ApiResponse<Void> changePassword(
            @Valid @RequestBody UserDto.ChangePasswordRequest request,
            @AuthenticationPrincipal User user) {
        userService.changePassword(request, user);
        return ApiResponse.success("密码修改成功", null);
    }

    @GetMapping("/me/settings")
    public ApiResponse<UserDto.UserSettings> getSettings(@AuthenticationPrincipal User user) {
        return ApiResponse.success(userService.getSettings(user));
    }

    @PutMapping("/me/settings")
    public ApiResponse<UserDto.UserSettings> updateSettings(
            @RequestBody UserDto.UpdateSettingsRequest request,
            @AuthenticationPrincipal User user) {
        return ApiResponse.success("设置更新成功", userService.updateSettings(request, user));
    }

    @GetMapping("/me/login-records")
    public ApiResponse<List<UserDto.LoginRecordResponse>> getLoginRecords(@AuthenticationPrincipal User user) {
        return ApiResponse.success(userService.getLoginRecords(user));
    }
}
