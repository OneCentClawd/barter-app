package com.barter.dto;

import com.barter.entity.TradeRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

public class TradeDto {

    @Data
    public static class CreateRequest {
        @NotNull(message = "目标物品ID不能为空")
        private Long targetItemId;

        @NotNull(message = "交换物品ID不能为空")
        private Long offeredItemId;

        private String message;
    }

    @Data
    public static class TradeResponse {
        private Long id;
        private ItemDto.ItemListResponse targetItem;
        private ItemDto.ItemListResponse offeredItem;
        private ItemDto.UserBrief requester;
        private String message;
        private TradeRequest.TradeStatus status;
        private LocalDateTime createdAt;
    }

    @Data
    public static class StatusUpdateRequest {
        @NotNull
        private TradeRequest.TradeStatus status;
    }
}
