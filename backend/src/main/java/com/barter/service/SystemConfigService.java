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

    public boolean isAllowUserViewItems() {
        return configRepository.findByConfigKey(SystemConfig.ALLOW_USER_VIEW_ITEMS)
                .map(config -> "true".equalsIgnoreCase(config.getConfigValue()))
                .orElse(false);  // 默认不允许
    }

    @Transactional
    public void setAllowUserChat(boolean allow) {
        setConfig(SystemConfig.ALLOW_USER_CHAT, allow, "是否允许普通用户之间聊天");
    }

    @Transactional
    public void setAllowUserViewItems(boolean allow) {
        setConfig(SystemConfig.ALLOW_USER_VIEW_ITEMS, allow, "是否允许用户看到其他用户的物品");
    }

    private void setConfig(String key, boolean value, String description) {
        SystemConfig config = configRepository.findByConfigKey(key)
                .orElseGet(() -> {
                    SystemConfig newConfig = new SystemConfig();
                    newConfig.setConfigKey(key);
                    newConfig.setDescription(description);
                    return newConfig;
                });
        config.setConfigValue(String.valueOf(value));
        configRepository.save(config);
    }

    public String getConfig(String key) {
        return configRepository.findByConfigKey(key)
                .map(SystemConfig::getConfigValue)
                .orElse(null);
    }
}
