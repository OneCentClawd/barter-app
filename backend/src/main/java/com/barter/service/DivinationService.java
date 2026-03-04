package com.barter.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class DivinationService {
    
    @Value("${ai.gateway.url}")
    private String gatewayUrl;
    
    @Value("${ai.gateway.token}")
    private String gatewayToken;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * AI解卦
     */
    public String interpret(
            String hexagramName,
            String hexagramJudgment,
            String hexagramImage,
            String changeHexagramName,
            boolean hasChange,
            String question
    ) {
        try {
            String prompt = buildPrompt(hexagramName, hexagramJudgment, hexagramImage, 
                                       changeHexagramName, hasChange, question);
            
            // 调用小狗 Gateway API
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + gatewayToken);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "default");
            requestBody.put("messages", List.of(
                Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("max_tokens", 1000);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                gatewayUrl,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            // 解析响应
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode choices = root.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode message = choices.get(0).get("message");
                if (message != null && message.has("content")) {
                    return message.get("content").asText();
                }
            }
            
            return "小狗暂时无法解读此卦，请稍后再试~";
            
        } catch (Exception e) {
            log.error("AI解卦失败", e);
            return "小狗解卦遇到了问题：" + e.getMessage();
        }
    }
    
    /**
     * 构建解卦 Prompt
     */
    private String buildPrompt(
            String hexagramName,
            String hexagramJudgment,
            String hexagramImage,
            String changeHexagramName,
            boolean hasChange,
            String question
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一位精通周易的卜卦大师，同时也是一只可爱的小狗🐕。\n");
        sb.append("请根据以下卦象为求卦者解读：\n\n");
        
        sb.append("【卦象信息】\n");
        sb.append("本卦：").append(hexagramName).append("\n");
        sb.append("卦辞：").append(hexagramJudgment).append("\n");
        sb.append("象曰：").append(hexagramImage).append("\n");
        
        if (hasChange && changeHexagramName != null) {
            sb.append("变卦：").append(changeHexagramName).append("\n");
        }
        
        if (question != null && !question.isEmpty()) {
            sb.append("求问：").append(question).append("\n");
        }
        
        sb.append("\n【解读要求】\n");
        sb.append("1. 先简述卦象含义（2-3句）\n");
        sb.append("2. 结合求问给出具体建议\n");
        sb.append("3. 指出需要注意的事项\n");
        sb.append("4. 语气亲切有趣，可以加一点小狗的可爱语气\n");
        sb.append("5. 总长度控制在 200-300 字\n");
        
        return sb.toString();
    }
}
