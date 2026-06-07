package com.chatbot.module.auth.controller;

import com.chatbot.common.Result;
import com.chatbot.dao.entity.SysUser;
import com.chatbot.dao.mapper.SysUserMapper;
import com.chatbot.module.auth.dto.LoginRequest;
import com.chatbot.module.auth.dto.LoginResponse;
import com.chatbot.module.auth.service.AuthService;
import com.chatbot.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "认证管理", description = "登录/登出/Token刷新")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final SysUserMapper sysUserMapper;

    public AuthController(AuthService authService, JwtUtil jwtUtil, SysUserMapper sysUserMapper) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
        this.sysUserMapper = sysUserMapper;
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新Token")
    public Result<LoginResponse> refresh(@RequestBody Map<String, String> body) {
        return Result.ok(null);
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息")
    public Result<LoginResponse.UserInfo> me(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.parseToken(token).get("userId", Long.class);
        return Result.ok(authService.getCurrentUser(userId));
    }

    @PostMapping("/logout")
    @Operation(summary = "登出")
    public Result<Void> logout() {
        return Result.ok();
    }
}