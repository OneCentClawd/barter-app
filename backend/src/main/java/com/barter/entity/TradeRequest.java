package com.barter.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
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

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum TradeStatus {
        PENDING,    // 等待对方确认
        ACCEPTED,   // 已接受
        REJECTED,   // 已拒绝
        COMPLETED,  // 交易完成
        CANCELLED   // 已取消
    }
}
