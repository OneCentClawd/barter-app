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
@Table(name = "login_records")
public class LoginRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String ipAddress;       // 登录 IP
    private String userAgent;       // 设备信息
    private String deviceType;      // 设备类型: Android/iOS/Web
    private String deviceModel;     // 设备型号
    private String appVersion;      // App 版本

    private Boolean success = true; // 是否登录成功
    private String failReason;      // 失败原因

    private LocalDateTime loginTime = LocalDateTime.now();
}
