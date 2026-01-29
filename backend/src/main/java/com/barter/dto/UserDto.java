package com.barter.dto;

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
        private LocalDateTime createdAt;
    }

    @Data
    public static class UpdateProfileRequest {
        private String nickname;
        private String phone;
        private String bio;
    }
}
