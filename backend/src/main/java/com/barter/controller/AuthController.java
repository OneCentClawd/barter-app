package com.barter.controller;

import com.barter.dto.ApiResponse;
import com.barter.dto.AuthDto;
import com.barter.service.AuthService;
import com.barter.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;

    @PostMapping("/send-code")
    public ApiResponse<Void> sendVerificationCode(@Valid @RequestBody AuthDto.SendCodeRequest request) {
        emailService.sendVerificationCode(request.getEmail());
        return ApiResponse.success("验证码已发送", null);
    }
    
    @PostMapping("/send-login-code")
    public ApiResponse<Void> sendLoginCode(@Valid @RequestBody AuthDto.SendCodeRequest request) {
        emailService.sendLoginCode(request.getEmail());
        return ApiResponse.success("验证码已发送", null);
    }

    @PostMapping("/register")
    public ApiResponse<AuthDto.AuthResponse> register(
            @Valid @RequestBody AuthDto.RegisterRequest request,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        return ApiResponse.success("注册成功", authService.register(request, ipAddress, userAgent));
    }

    @PostMapping("/login")
    public ApiResponse<AuthDto.AuthResponse> login(
            @Valid @RequestBody AuthDto.LoginRequest request,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        return ApiResponse.success("登录成功", authService.login(request, ipAddress, userAgent));
    }
    
    @PostMapping("/login/email")
    public ApiResponse<AuthDto.AuthResponse> loginWithEmail(
            @Valid @RequestBody AuthDto.EmailLoginRequest request,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        return ApiResponse.success("登录成功", authService.loginWithEmail(request, ipAddress, userAgent));
    }
    
    @PostMapping("/login/code")
    public ApiResponse<AuthDto.AuthResponse> loginWithCode(
            @Valid @RequestBody AuthDto.CodeLoginRequest request,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        return ApiResponse.success("登录成功", authService.loginWithCode(request, ipAddress, userAgent));
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
