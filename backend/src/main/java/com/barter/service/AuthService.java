package com.barter.service;

import com.barter.dto.AuthDto;
import com.barter.entity.LoginRecord;
import com.barter.entity.User;
import com.barter.repository.LoginRecordRepository;
import com.barter.repository.UserRepository;
import com.barter.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final LoginRecordRepository loginRecordRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final EmailService emailService;
    private final WalletService walletService;

    // 推荐奖励积分
    private static final int REFERRAL_REWARD_POINTS = 50;

    @Transactional
    public AuthDto.AuthResponse register(AuthDto.RegisterRequest request, String ipAddress, String userAgent) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已被使用");
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 验证邮箱验证码
        if (!emailService.verifyCode(request.getEmail(), request.getVerificationCode())) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 检查推荐人
        User referrer = null;
        if (request.getReferrerId() != null) {
            referrer = userRepository.findById(request.getReferrerId()).orElse(null);
            if (referrer == null) {
                throw new RuntimeException("推荐人不存在");
            }
        }

        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setTokenVersion(1L);
        user.setEmailVerified(true);
        user.setReferrer(referrer);

        user = userRepository.save(user);

        // 给推荐人发放奖励
        if (referrer != null) {
            walletService.addReferralReward(referrer, user, REFERRAL_REWARD_POINTS);
        }

        // 记录登录信息
        saveLoginRecord(user, ipAddress, userAgent, true, null);

        // 生成 JWT（包含 tokenVersion）
        String token = tokenProvider.generateToken(user.getId(), user.getUsername(), user.getTokenVersion());

        return new AuthDto.AuthResponse(token, user.getId(), user.getUsername(),
                user.getNickname(), user.getAvatar());
    }

    @Transactional
    public AuthDto.AuthResponse login(AuthDto.LoginRequest request, String ipAddress, String userAgent) {
        // 查找用户（用户名）
        User user = userRepository.findByUsername(request.getUsername()).orElse(null);
        
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            saveLoginRecord(user, ipAddress, userAgent, false, "密码错误");
            throw new RuntimeException("用户名或密码错误");
        }

        return doLogin(user, ipAddress, userAgent);
    }
    
    @Transactional
    public AuthDto.AuthResponse loginWithEmail(AuthDto.EmailLoginRequest request, String ipAddress, String userAgent) {
        // 查找用户（邮箱）
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        
        if (user == null) {
            throw new RuntimeException("邮箱或密码错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            saveLoginRecord(user, ipAddress, userAgent, false, "密码错误");
            throw new RuntimeException("邮箱或密码错误");
        }

        return doLogin(user, ipAddress, userAgent);
    }
    
    @Transactional
    public AuthDto.AuthResponse loginWithCode(AuthDto.CodeLoginRequest request, String ipAddress, String userAgent) {
        // 查找用户（邮箱）
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        
        if (user == null) {
            throw new RuntimeException("该邮箱未注册");
        }

        // 验证验证码
        if (!emailService.verifyLoginCode(request.getEmail(), request.getCode())) {
            saveLoginRecord(user, ipAddress, userAgent, false, "验证码错误");
            throw new RuntimeException("验证码错误或已过期");
        }

        return doLogin(user, ipAddress, userAgent);
    }
    
    private AuthDto.AuthResponse doLogin(User user, String ipAddress, String userAgent) {
        // 递增 tokenVersion，使旧 token 失效
        user.setTokenVersion(user.getTokenVersion() == null ? 1L : user.getTokenVersion() + 1);
        userRepository.save(user);

        // 记录成功的登录
        saveLoginRecord(user, ipAddress, userAgent, true, null);

        // 生成 JWT（包含新的 tokenVersion）
        String token = tokenProvider.generateToken(user.getId(), user.getUsername(), user.getTokenVersion());

        return new AuthDto.AuthResponse(token, user.getId(), user.getUsername(),
                user.getNickname(), user.getAvatar());
    }

    private void saveLoginRecord(User user, String ipAddress, String userAgent, boolean success, String failReason) {
        LoginRecord record = new LoginRecord();
        record.setUser(user);
        record.setIpAddress(ipAddress);
        record.setUserAgent(userAgent);
        record.setDeviceType(parseDeviceType(userAgent));
        record.setSuccess(success);
        record.setFailReason(failReason);
        loginRecordRepository.save(record);
    }

    private String parseDeviceType(String userAgent) {
        if (userAgent == null) return "Unknown";
        userAgent = userAgent.toLowerCase();
        if (userAgent.contains("android")) return "Android";
        if (userAgent.contains("iphone") || userAgent.contains("ipad")) return "iOS";
        if (userAgent.contains("windows")) return "Windows";
        if (userAgent.contains("mac")) return "Mac";
        return "Other";
    }
}
