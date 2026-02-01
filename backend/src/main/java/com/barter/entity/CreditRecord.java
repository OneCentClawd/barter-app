package com.barter.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "credit_records")
public class CreditRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CreditChangeType type;

    // 分数变化（正数增加，负数减少）
    @Column(nullable = false)
    private Integer scoreChange;

    // 变化后的信用分
    @Column(nullable = false)
    private Integer scoreAfter;

    // 说明
    private String description;

    // 关联ID（如交易ID）
    private Long relatedId;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum CreditChangeType {
        INITIAL,            // 初始信用分
        TRADE_COMPLETE,     // 完成交易 +5
        GOOD_REVIEW,        // 获得好评 +3
        ON_TIME_SHIP,       // 按时发货 +1
        TRADE_CANCEL,       // 取消交易 -10
        LATE_SHIP,          // 超时不发货 -25
        BAD_REVIEW,         // 获得差评 -8
        REPORT_CONFIRMED,   // 举报成立 -40
        DEPOSIT_DEFAULT     // 保证金违约 -50
    }
}
