package com.barter.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "items")
@EqualsAndHashCode(exclude = "items")
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String nickname;
    private String avatar;
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private Double rating = 5.0;
    private Integer ratingCount = 0;
    
    // 信用分系统
    private Integer creditScore = 100;  // 初始信用分100

    // 用户设置
    private Boolean showPhoneToOthers = true;
    private Boolean allowStrangersMessage = true;
    private Boolean notifyNewMessage = true;
    private Boolean notifyTradeUpdate = true;
    private Boolean notifySystemAnnouncement = true;

    // 单设备登录：token 版本号，每次登录递增，旧 token 失效
    private Long tokenVersion = 0L;

    // 管理员标识
    private Boolean isAdmin = false;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Item> items;
}
