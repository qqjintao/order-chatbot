package com.chatbot.module.auth.service;

import com.chatbot.common.BusinessException;
import com.chatbot.common.HttpStatusEnum;
import com.chatbot.dao.entity.SysUser;
import com.chatbot.dao.mapper.SysUserMapper;
import com.chatbot.module.auth.dto.LoginRequest;
import com.chatbot.module.auth.dto.LoginResponse;
import com.chatbot.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final SysUserMapper sysUserMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.expiration}")
    private long expiration;

    public AuthService(SysUserMapper sysUserMapper, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void initDefaultUsers() {
        if (sysUserMapper.selectCount(null) == 0) {
            SysUser admin = new SysUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setRealName("管理员");
            admin.setRole("admin");
            admin.setStatus(1);
            admin.setCreateTime(LocalDateTime.now());
            sysUserMapper.insert(admin);

            SysUser agent = new SysUser();
            agent.setUsername("agent");
            agent.setPassword(passwordEncoder.encode("123456"));
            agent.setRealName("客服坐席");
            agent.setRole("agent");
            agent.setStatus(1);
            agent.setCreateTime(LocalDateTime.now());
            sysUserMapper.insert(agent);

            SysUser user = new SysUser();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("123456"));
            user.setRealName("普通用户");
            user.setRole("user");
            user.setStatus(1);
            user.setCreateTime(LocalDateTime.now());
            sysUserMapper.insert(user);
        }
    }

    public LoginResponse login(LoginRequest request) {
        SysUser user = sysUserMapper.findByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException(HttpStatusEnum.UNAUTHORIZED, "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(HttpStatusEnum.FORBIDDEN, "账户已被禁用");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(HttpStatusEnum.UNAUTHORIZED, "用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = UUID.randomUUID().toString().replace("-", "");

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(expiration);

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setRole(user.getRole());
        userInfo.setAvatar(user.getAvatar());
        response.setUserInfo(userInfo);

        return response;
    }

    public LoginResponse.UserInfo getCurrentUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(HttpStatusEnum.NOT_FOUND, "用户不存在");
        }
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setRole(user.getRole());
        userInfo.setAvatar(user.getAvatar());
        return userInfo;
    }
}