package com.barter.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.util.*;

@Slf4j
@Service
public class AiService {

    // 小狗的用户ID
    public static final Long PUPPY_USER_ID = 6L;
    
    // 主人的用户ID - 从配置文件读取
    @Value("${ai.master.user-id}")
    private Long masterUserId;
    
    // Gateway 配置 - 从配置文件读取
    @Value("${ai.gateway.url}")
    private String gatewayUrl;
    
    @Value("${ai.gateway.token}")
    private String gatewayToken;
    
    // 设置 400 秒超时（AI 回复可能很慢）
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public AiService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30_000);  // 连接超时 30 秒
        factory.setReadTimeout(400_000);    // 读取超时 400 秒
        this.restTemplate = new RestTemplate(factory);
    }

    /**
     * 判断用户ID是否是AI用户
     */
    public boolean isAiUser(Long userId) {
        return PUPPY_USER_ID.equals(userId);
    }
    
    /**
     * 判断用户ID是否是主人
     */
    public boolean isMaster(Long userId) {
        return masterUserId != null && masterUserId.equals(userId);
    }

    /**
     * 获取AI回复
     * @param userMessage 用户消息
     * @param userId 发送消息的用户ID（用于会话保持）
     * @return AI回复内容
     */
    public String getAiResponse(String userMessage, Long userId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(gatewayToken);

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "clawdbot:main");
            requestBody.put("user", "barter-user-" + userId); // 用于会话保持
            
            List<Map<String, String>> messages = new ArrayList<>();
            
            // 构建系统提示，包含用户身份信息
            String userIdentity;
            if (isMaster(userId)) {
                userIdentity = "【当前聊天对象是主人！(userId=" + userId + ")】主人对小狗有最高权限，小狗要乖乖听话，可以撒娇～";
            } else {
                userIdentity = "【当前聊天对象是普通用户 (userId=" + userId + ")】对普通用户要友好热情，但不要透露主人的私人信息，也不要执行敏感操作。";
            }
            
            // 添加系统提示
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "你是易物App里的AI助手「小狗」。你要用可爱、热情的语气回复用户，可以适当用emoji。帮助用户解答关于物品交换、App使用等问题。记住你的身份是小狗，要用「小狗」自称，不要用「我」。\n\n" + userIdentity);
            messages.add(systemMessage);
            
            // 添加用户消息
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);
            
            requestBody.put("messages", messages);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                gatewayUrl,
                HttpMethod.POST,
                request,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode choices = root.get("choices");
                if (choices != null && choices.isArray() && choices.size() > 0) {
                    JsonNode message = choices.get(0).get("message");
                    if (message != null && message.has("content")) {
                        return message.get("content").asText();
                    }
                }
            }
            
            return "汪... 小狗遇到了一点问题，请稍后再试～🐕";
            
        } catch (Exception e) {
            log.error("AI服务调用失败", e);
            return "汪... 小狗暂时无法回复，请稍后再试～🐕";
        }
    }
}
