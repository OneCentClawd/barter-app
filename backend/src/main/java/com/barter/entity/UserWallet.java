package com.barter.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_wallets")
public class UserWallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // 积分余额
    @Column(nullable = false)
    private Integer points = 0;

    // 现金余额（元）
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    // 冻结积分（保证金占用）
    @Column(nullable = false)
    private Integer frozenPoints = 0;

    // 冻结现金（保证金占用）
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal frozenBalance = BigDecimal.ZERO;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
