package com.chatbot.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HTTP状态码枚举测试")
class HttpStatusEnumTest {

    @Test
    @DisplayName("应返回正确的状态码")
    void testCodes() {
        assertEquals(200, HttpStatusEnum.OK.getCode());
        assertEquals(400, HttpStatusEnum.BAD_REQUEST.getCode());
        assertEquals(401, HttpStatusEnum.UNAUTHORIZED.getCode());
        assertEquals(403, HttpStatusEnum.FORBIDDEN.getCode());
        assertEquals(404, HttpStatusEnum.NOT_FOUND.getCode());
        assertEquals(409, HttpStatusEnum.CONFLICT.getCode());
        assertEquals(429, HttpStatusEnum.TOO_MANY_REQUESTS.getCode());
        assertEquals(500, HttpStatusEnum.INTERNAL_ERROR.getCode());
        assertEquals(503, HttpStatusEnum.SERVICE_UNAVAILABLE.getCode());
    }

    @Test
    @DisplayName("应返回正确的消息")
    void testMessages() {
        assertEquals("success", HttpStatusEnum.OK.getMessage());
        assertEquals("请求参数错误", HttpStatusEnum.BAD_REQUEST.getMessage());
        assertEquals("未授权", HttpStatusEnum.UNAUTHORIZED.getMessage());
        assertEquals("无权限", HttpStatusEnum.FORBIDDEN.getMessage());
        assertEquals("资源未找到", HttpStatusEnum.NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("业务状态码应正确")
    void testBusinessCodes() {
        assertEquals(1001, HttpStatusEnum.SESSION_CLOSED.getCode());
        assertEquals(1002, HttpStatusEnum.AI_TIMEOUT.getCode());
        assertEquals(1003, HttpStatusEnum.KNOWLEDGE_NOT_FOUND.getCode());
    }
}