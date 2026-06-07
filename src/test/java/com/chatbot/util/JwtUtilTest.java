package com.chatbot.util;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JWT工具类测试")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        // 使用Base64编码的密钥进行测试
        jwtUtil = new JwtUtil("Y2hhdGJvdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tZ2VuZXJhdGlvbi0yMDI2", 3600);
    }

    @Test
    @DisplayName("生成Token - 应返回非空字符串")
    void testGenerateToken() {
        String token = jwtUtil.generateToken(1L, "admin", "admin");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("解析Token - 应返回正确的Claims")
    void testParseToken() {
        String token = jwtUtil.generateToken(1L, "testuser", "user");
        Claims claims = jwtUtil.parseToken(token);
        assertEquals("testuser", claims.getSubject());
        assertEquals(1L, claims.get("userId", Long.class));
        assertEquals("user", claims.get("role", String.class));
    }

    @Test
    @DisplayName("验证Token - 有效Token应返回true")
    void testValidateTokenValid() {
        String token = jwtUtil.generateToken(1L, "admin", "admin");
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    @DisplayName("验证Token - 无效Token应返回false")
    void testValidateTokenInvalid() {
        assertFalse(jwtUtil.validateToken("invalid.token.here"));
        assertFalse(jwtUtil.validateToken(""));
    }

    @Test
    @DisplayName("不同用户Token应不同")
    void testTokensAreDifferentForDifferentUsers() {
        String token1 = jwtUtil.generateToken(1L, "admin", "admin");
        String token2 = jwtUtil.generateToken(2L, "agent", "agent");
        assertNotEquals(token1, token2);
    }
}