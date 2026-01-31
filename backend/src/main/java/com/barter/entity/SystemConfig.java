package com.barter.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "system_config")
public class SystemConfig {

    @Id
    private String configKey;

    private String configValue;

    private String description;

    // 预定义的配置键
    public static final String ALLOW_USER_CHAT = "allow_user_chat";  // 是否允许用户之间聊天
}
