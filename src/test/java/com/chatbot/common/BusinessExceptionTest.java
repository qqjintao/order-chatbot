package com.chatbot.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("业务异常类测试")
class BusinessExceptionTest {

    @Test
    @DisplayName("message构造 - 应使用默认错误码500")
    void testConstructorWithMessage() {
        BusinessException ex = new BusinessException("业务处理失败");
        assertEquals(500, ex.getCode());
        assertEquals("业务处理失败", ex.getMessage());
    }

    @Test
    @DisplayName("code+message构造 - 应使用自定义错误码")
    void testConstructorWithCodeAndMessage() {
        BusinessException ex = new BusinessException(400, "参数校验失败");
        assertEquals(400, ex.getCode());
        assertEquals("参数校验失败", ex.getMessage());
    }

    @Test
    @DisplayName("HttpStatusEnum构造 - 应使用枚举值")
    void testConstructorWithHttpStatus() {
        BusinessException ex = new BusinessException(HttpStatusEnum.NOT_FOUND);
        assertEquals(404, ex.getCode());
        assertEquals("资源未找到", ex.getMessage());
    }

    @Test
    @DisplayName("HttpStatusEnum+message构造 - 应使用枚举码和自定义消息")
    void testConstructorWithHttpStatusAndMessage() {
        BusinessException ex = new BusinessException(HttpStatusEnum.UNAUTHORIZED, "Token已过期");
        assertEquals(401, ex.getCode());
        assertEquals("Token已过期", ex.getMessage());
    }

    @Test
    @DisplayName("应继承RuntimeException")
    void testIsRuntimeException() {
        BusinessException ex = new BusinessException("test");
        assertTrue(ex instanceof RuntimeException);
    }
}