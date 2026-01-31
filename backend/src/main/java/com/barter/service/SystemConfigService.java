package com.barter.service;

import com.barter.entity.SystemConfig;
import com.barter.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final SystemConfigRepository configRepository;

    public boolean isAllowUserChat() {
        return configRepository.findByConfigKey(SystemConfig.ALLOW_USER_CHAT)
                .map(config -> "true".equalsIgnoreCase(config.getConfigValue()))
                .orElse(false);  // 默认不允许
    }

    @Transactional
    public void setAllowUserChat(boolean allow) {
        SystemConfig config = configRepository.findByConfigKey(SystemConfig.ALLOW_USER_CHAT)
                .orElseGet(() -> {
                    SystemConfig newConfig = new SystemConfig();
                    newConfig.setConfigKey(SystemConfig.ALLOW_USER_CHAT);
                    newConfig.setDescription("是否允许普通用户之间聊天");
                    return newConfig;
                });
        config.setConfigValue(String.valueOf(allow));
        configRepository.save(config);
    }

    public String getConfig(String key) {
        return configRepository.findByConfigKey(key)
                .map(SystemConfig::getConfigValue)
                .orElse(null);
    }
}
