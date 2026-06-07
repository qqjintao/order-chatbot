package com.chatbot.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("统一响应体Result测试")
class ResultTest {

    @Test
    @DisplayName("ok() - 应返回200状态码和成功消息")
    void testOk() {
        Result<String> result = Result.ok("test");
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals("test", result.getData());
        assertNotNull(result.getTimestamp());
    }

    @Test
    @DisplayName("ok()无数据 - 应返回200且data为null")
    void testOkWithoutData() {
        Result<Void> result = Result.ok();
        assertEquals(200, result.getCode());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("ok(message, data) - 应返回自定义消息")
    void testOkWithMessage() {
        Result<String> result = Result.ok("操作成功", "data");
        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMessage());
        assertEquals("data", result.getData());
    }

    @Test
    @DisplayName("fail() - 应返回500且默认消息")
    void testFail() {
        Result<Void> result = Result.fail();
        assertEquals(500, result.getCode());
        assertEquals("服务器内部错误", result.getMessage());
    }

    @Test
    @DisplayName("fail(message) - 应返回500且自定义消息")
    void testFailWithMessage() {
        Result<Void> result = Result.fail("数据库连接失败");
        assertEquals(500, result.getCode());
        assertEquals("数据库连接失败", result.getMessage());
    }

    @Test
    @DisplayName("fail(code, message) - 应返回自定义状态码和消息")
    void testFailWithCodeAndMessage() {
        Result<Void> result = Result.fail(404, "资源未找到");
        assertEquals(404, result.getCode());
        assertEquals("资源未找到", result.getMessage());
    }

    @Test
    @DisplayName("fail(HttpStatusEnum) - 应返回对应枚举的状态码和消息")
    void testFailWithHttpStatus() {
        Result<Void> result = Result.fail(HttpStatusEnum.UNAUTHORIZED);
        assertEquals(401, result.getCode());
        assertEquals("未授权", result.getMessage());
    }

    @Test
    @DisplayName("JSON序列化 - 应排除null值")
    void testJsonIncludeNonNull() {
        Result<Void> result = Result.ok();
        assertNull(result.getData());
    }
}