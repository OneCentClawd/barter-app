package com.barter.controller;

import com.barter.service.DivinationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/divination")
@CrossOrigin(origins = "*")
public class DivinationController {
    
    @Autowired
    private DivinationService divinationService;
    
    /**
     * AI解卦接口
     */
    @PostMapping("/interpret")
    public ResponseEntity<?> interpret(@RequestBody Map<String, Object> request) {
        try {
            String hexagramName = (String) request.get("hexagramName");
            String hexagramJudgment = (String) request.get("hexagramJudgment");
            String hexagramImage = (String) request.get("hexagramImage");
            String changeHexagramName = (String) request.get("changeHexagramName");
            Boolean hasChange = (Boolean) request.get("hasChange");
            String question = (String) request.get("question");
            
            // 检查权限
            // TODO: 从请求中获取用户ID，检查enableList
            // 目前 ignoreFilter = true，所有人都可用
            
            String interpretation = divinationService.interpret(
                hexagramName, 
                hexagramJudgment, 
                hexagramImage,
                changeHexagramName,
                hasChange != null && hasChange,
                question
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "interpretation", interpretation
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * 获取配置（是否开启AI解卦）
     */
    @GetMapping("/config")
    public ResponseEntity<?> getConfig() {
        return ResponseEntity.ok(Map.of(
            "ignoreFilter", true,  // 暂时所有人可用
            "enabled", true
        ));
    }
}
