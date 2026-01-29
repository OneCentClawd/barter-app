package com.barter.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

public class ReviewDto {

    @Data
    public static class CreateRequest {
        @NotNull(message = "交易ID不能为空")
        private Long tradeRequestId;

        @NotNull(message = "评分不能为空")
        @Min(value = 1, message = "评分最低1分")
        @Max(value = 5, message = "评分最高5分")
        private Integer rating;

        private String comment;
    }

    @Data
    public static class ReviewResponse {
        private Long id;
        private ItemDto.UserBrief reviewer;
        private Integer rating;
        private String comment;
        private LocalDateTime createdAt;
    }
}
