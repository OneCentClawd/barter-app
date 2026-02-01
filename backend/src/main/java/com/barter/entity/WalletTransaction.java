package com.barter.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wallet_transactions")
public class WalletTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    // 积分变化（正数增加，负数减少）
    private Integer pointsChange = 0;

    // 现金变化
    @Column(precision = 10, scale = 2)
    private BigDecimal balanceChange = BigDecimal.ZERO;

    // 交易后积分余额
    private Integer pointsAfter;

    // 交易后现金余额
    @Column(precision = 10, scale = 2)
    private BigDecimal balanceAfter;

    // 说明
    private String description;

    // 关联ID（如交易ID）
    private Long relatedId;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum TransactionType {
        RECHARGE,           // 充值
        WITHDRAW,           // 提现
        SIGN_IN,            // 签到奖励
        TRADE_REWARD,       // 交易奖励
        DEPOSIT_FREEZE,     // 保证金冻结
        DEPOSIT_UNFREEZE,   // 保证金解冻
        DEPOSIT_FORFEIT,    // 保证金没收
        DEPOSIT_RECEIVE,    // 收到赔偿
        INVITE_REWARD       // 邀请奖励
    }
}
