package com.icecream.backend.controller;

import com.icecream.backend.dto.ApiResponse;
import com.icecream.backend.dto.request.LoginRequest;
import com.icecream.backend.dto.request.RefreshTokenRequest;
import com.icecream.backend.dto.request.RegisterRequest;
import com.icecream.backend.dto.response.AuthResponse;
import com.icecream.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * 认证控制器
 * 处理用户注册、登录、令牌刷新等认证相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户认证相关接口，包括注册、登录、令牌刷新等")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册接口，注册成功后返回JWT令牌")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("用户注册: username={}", request.getUsername());
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("注册成功", response));
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录接口，登录成功后返回JWT令牌")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("用户登录: usernameOrEmail={}", request.getUsernameOrEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("登录成功", response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("刷新令牌请求");
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("令牌刷新成功", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户登出，使当前令牌失效")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest httpRequest) {
        // 从请求头中获取令牌
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            // 登出逻辑由AuthService处理
            authService.logout(accessToken, null);
        }

        log.info("用户登出");
        return ResponseEntity.ok(ApiResponse.success("登出成功"));
    }
}