package com.barter.dto;

import com.barter.entity.CreditRecord;
import com.barter.entity.WalletTransaction;
import com.barter.service.CreditService;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WalletDto {

    @Data
    public static class WalletResponse {
        private Integer points;             // 积分余额
        private BigDecimal balance;         // 现金余额
        private Integer frozenPoints;       // 冻结积分
        private BigDecimal frozenBalance;   // 冻结现金
        private Integer availablePoints;    // 可用积分
        private BigDecimal availableBalance; // 可用现金
    }

    @Data
    public static class TransactionResponse {
        private Long id;
        private WalletTransaction.TransactionType type;
        private Integer pointsChange;
        private BigDecimal balanceChange;
        private String description;
        private LocalDateTime createdAt;
    }

    @Data
    public static class CreditResponse {
        private Integer creditScore;
        private CreditService.CreditLevel level;
        private String levelName;
        private Double depositRatio;        // 保证金比例
        private Boolean canRemoteTrade;     // 是否可远程交易
        private Integer nextLevelScore;     // 下一等级所需分数
    }

    @Data
    public static class CreditRecordResponse {
        private Long id;
        private CreditRecord.CreditChangeType type;
        private Integer scoreChange;
        private Integer scoreAfter;
        private String description;
        private LocalDateTime createdAt;
    }

    @Data
    public static class RechargeRequest {
        private BigDecimal amount;
    }

    @Data
    public static class DepositCalculation {
        private BigDecimal totalAmount;     // 保证金总额
        private Double ratio;               // 实际比例
        private Integer pointsNeeded;       // 需要积分
        private BigDecimal cashNeeded;      // 需要现金（积分不足部分）
        private Boolean canAfford;          // 是否支付得起
    }
}
