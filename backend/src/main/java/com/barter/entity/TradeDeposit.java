package com.barter.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "trade_deposits")
public class TradeDeposit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_request_id", nullable = false)
    private TradeRequest tradeRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 积分支付部分
    @Column(nullable = false)
    private Integer pointsAmount = 0;

    // 现金支付部分
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cashAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DepositStatus status = DepositStatus.PENDING;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum DepositStatus {
        PENDING,    // 待支付
        FROZEN,     // 已冻结
        REFUNDED,   // 已退还
        FORFEITED   // 已没收
    }
}
