package com.barter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

public class UserDto {

    @Data
    public static class ProfileResponse {
        private Long id;
        private String username;
        private String email;
        private String nickname;
        private String avatar;
        private String phone;
        private String bio;
        private Double rating;
        private Integer ratingCount;
        private Integer itemCount;
        private Integer tradeCount;
        private Boolean isAdmin;
        private Integer creditScore;        // 信用分
        private String creditLevel;         // 信用等级
        private LocalDateTime createdAt;
        // 用户设置
        private UserSettings settings;
    }

    @Data
    public static class UpdateProfileRequest {
        private String nickname;
        private String phone;
        private String bio;
    }

    @Data
    public static class ChangePasswordRequest {
        @NotBlank(message = "当前密码不能为空")
        private String oldPassword;
        
        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, message = "密码至少6位")
        private String newPassword;
    }

    @Data
    public static class UserSettings {
        private Boolean showPhoneToOthers = true;
        private Boolean allowStrangersMessage = true;
        private Boolean notifyNewMessage = true;
        private Boolean notifyTradeUpdate = true;
        private Boolean notifySystemAnnouncement = true;
    }

    @Data
    public static class UpdateSettingsRequest {
        private Boolean showPhoneToOthers;
        private Boolean allowStrangersMessage;
        private Boolean notifyNewMessage;
        private Boolean notifyTradeUpdate;
        private Boolean notifySystemAnnouncement;
    }

    @Data
    public static class LoginRecordResponse {
        private Long id;
        private String ipAddress;
        private String deviceType;
        private String userAgent;
        private Boolean success;
        private String failReason;
        private LocalDateTime loginTime;
    }

    @Data
    public static class RateUserRequest {
        private Integer rating;  // 1-5
        private String comment;
    }

    @Data
    public static class UserRatingResponse {
        private Long id;
        private Long raterId;
        private String raterNickname;
        private String raterAvatar;
        private Integer rating;
        private String comment;
        private LocalDateTime createdAt;
    }

    @Data
    public static class PublicProfileResponse {
        private Long id;
        private String username;
        private String nickname;
        private String avatar;
        private String bio;
        private Double rating;
        private Integer ratingCount;
        private Integer itemCount;
        private Boolean isAdmin;
        private LocalDateTime createdAt;
        // 当前用户对此用户的评分（如果有）
        private UserRatingResponse myRating;
    }
}
