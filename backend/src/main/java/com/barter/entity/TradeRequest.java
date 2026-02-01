package com.barter.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "trade_requests")
public class TradeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 发起交换请求的用户
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    // 目标物品（想要换的）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_item_id", nullable = false)
    private Item targetItem;

    // 用来交换的物品（自己的）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offered_item_id", nullable = false)
    private Item offeredItem;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    private TradeStatus status = TradeStatus.PENDING;

    // 双方确认完成
    private Boolean requesterConfirmed = false;  // 发起方确认
    private Boolean targetConfirmed = false;     // 接收方确认
    
    // 交易模式
    @Enumerated(EnumType.STRING)
    private TradeMode tradeMode = TradeMode.IN_PERSON;  // 默认面交
    
    // 远程交易相关
    @Column(precision = 10, scale = 2)
    private BigDecimal estimatedValue;  // 物品估值（用于计算保证金）
    
    private String requesterTrackingNo;   // 发起方物流单号
    private String targetTrackingNo;      // 接收方物流单号
    private LocalDateTime requesterShippedAt;  // 发起方发货时间
    private LocalDateTime targetShippedAt;     // 接收方发货时间
    
    // 保证金是否已支付
    private Boolean requesterDepositPaid = false;
    private Boolean targetDepositPaid = false;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum TradeStatus {
        PENDING,            // 等待对方确认
        ACCEPTED,           // 已接受（面交）/ 待支付保证金（远程）
        DEPOSIT_PAID,       // 保证金已支付，待发货
        SHIPPING,           // 双方已发货，运输中
        DELIVERED,          // 已送达，待确认收货
        COMPLETED,          // 交易完成
        REJECTED,           // 已拒绝
        CANCELLED           // 已取消
    }
    
    public enum TradeMode {
        IN_PERSON,  // 面交
        REMOTE      // 远程（物流）
    }
}
