package com.barter.dto;

import com.barter.entity.TradeRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TradeDto {

    @Data
    public static class CreateRequest {
        @NotNull(message = "目标物品ID不能为空")
        private Long targetItemId;

        @NotNull(message = "交换物品ID不能为空")
        private Long offeredItemId;

        private String message;
        
        // 远程交易相关
        private TradeRequest.TradeMode tradeMode;
        private BigDecimal estimatedValue;  // 物品估值（远程交易必填）
    }

    @Data
    public static class TradeResponse {
        private Long id;
        private ItemDto.ItemListResponse targetItem;
        private ItemDto.ItemListResponse offeredItem;
        private ItemDto.UserBrief requester;
        private String message;
        private TradeRequest.TradeStatus status;
        private Boolean requesterConfirmed;
        private Boolean targetConfirmed;
        
        // 交易模式
        private TradeRequest.TradeMode tradeMode;
        private BigDecimal estimatedValue;
        
        // 远程交易信息
        private String requesterTrackingNo;
        private String targetTrackingNo;
        private LocalDateTime requesterShippedAt;
        private LocalDateTime targetShippedAt;
        private Boolean requesterDepositPaid;
        private Boolean targetDepositPaid;
        
        private LocalDateTime createdAt;
    }

    @Data
    public static class StatusUpdateRequest {
        @NotNull
        private TradeRequest.TradeStatus status;
    }
    
    @Data
    public static class ShipRequest {
        @NotNull(message = "物流单号不能为空")
        private String trackingNo;
    }
    
    @Data
    public static class DepositCalculation {
        private BigDecimal totalAmount;     // 保证金总额
        private Double ratio;               // 实际比例
        private Integer pointsNeeded;       // 需要积分
        private BigDecimal cashNeeded;      // 需要现金
        private Boolean canAfford;          // 是否支付得起
    }
}
