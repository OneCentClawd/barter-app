package com.barter.service;

import com.barter.entity.EmailVerificationCode;
import com.barter.repository.EmailVerificationCodeRepository;
import com.barter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final EmailVerificationCodeRepository codeRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@barter.com}")
    private String fromEmail;

    // 验证码有效期（分钟）
    private static final int CODE_EXPIRY_MINUTES = 10;
    // 每小时最多发送次数
    private static final int MAX_SENDS_PER_HOUR = 5;

    /**
     * 发送注册验证码（邮箱必须未注册）
     */
    @Transactional
    public void sendVerificationCode(String email) {
        // 检查邮箱是否已注册
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("该邮箱已被注册");
        }

        sendCode(email, "注册");
    }
    
    /**
     * 发送登录验证码（邮箱必须已注册）
     */
    @Transactional
    public void sendLoginCode(String email) {
        // 检查邮箱是否已注册
        if (!userRepository.existsByEmail(email)) {
            throw new RuntimeException("该邮箱未注册");
        }

        sendCode(email, "登录");
    }
    
    private void sendCode(String email, String purpose) {
        // 检查发送频率
        long recentCount = codeRepository.countByEmailAndCreatedAtAfter(
                email, LocalDateTime.now().minusHours(1));
        if (recentCount >= MAX_SENDS_PER_HOUR) {
            throw new RuntimeException("发送过于频繁，请稍后再试");
        }

        // 生成6位验证码
        String code = generateCode();

        // 保存验证码
        EmailVerificationCode verification = new EmailVerificationCode();
        verification.setEmail(email);
        verification.setCode(code);
        verification.setExpiresAt(LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES));
        codeRepository.save(verification);

        // 发送邮件
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("易物 <" + fromEmail + ">");
            message.setTo(email);
            message.setSubject("【易物】" + purpose + "验证码");
            message.setText(String.format(
                    "您好！\n\n您正在进行%s操作，验证码是：%s\n\n验证码有效期为%d分钟。\n\n如非本人操作，请忽略此邮件。\n\n易物团队",
                    purpose, code, CODE_EXPIRY_MINUTES));
            log.info("准备发送{}验证码到: {}", purpose, email);
            mailSender.send(message);
            log.info("验证码已发送到: {}", email);
        } catch (Exception e) {
            log.error("发送邮件失败: {} - {}", e.getClass().getName(), e.getMessage());
            throw new RuntimeException("发送验证码失败，请稍后重试");
        }
    }

    /**
     * 验证注册验证码
     */
    @Transactional
    public boolean verifyCode(String email, String code) {
        return doVerifyCode(email, code);
    }
    
    /**
     * 验证登录验证码
     */
    @Transactional
    public boolean verifyLoginCode(String email, String code) {
        return doVerifyCode(email, code);
    }
    
    private boolean doVerifyCode(String email, String code) {
        var verification = codeRepository.findByEmailAndCodeAndUsedFalseAndExpiresAtAfter(
                email, code, LocalDateTime.now());
        
        if (verification.isPresent()) {
            // 标记为已使用
            verification.get().setUsed(true);
            codeRepository.save(verification.get());
            return true;
        }
        return false;
    }

    private String generateCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }
}
