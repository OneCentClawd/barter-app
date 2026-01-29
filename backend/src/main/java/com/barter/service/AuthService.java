package com.barter.service;

import com.barter.dto.AuthDto;
import com.barter.entity.User;
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
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public AuthDto.AuthResponse register(AuthDto.RegisterRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已被使用");
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());

        user = userRepository.save(user);

        // 生成 JWT
        String token = tokenProvider.generateToken(user.getId(), user.getUsername());

        return new AuthDto.AuthResponse(token, user.getId(), user.getUsername(),
                user.getNickname(), user.getAvatar());
    }

    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {
        // 查找用户
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 生成 JWT
        String token = tokenProvider.generateToken(user.getId(), user.getUsername());

        return new AuthDto.AuthResponse(token, user.getId(), user.getUsername(),
                user.getNickname(), user.getAvatar());
    }
}
