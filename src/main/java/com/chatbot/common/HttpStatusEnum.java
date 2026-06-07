package com.chatbot.common;

public enum HttpStatusEnum {

    OK(200, "success"),
    CREATED(201, "created"),
    NO_CONTENT(204, "no content"),

    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源未找到"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    CONFLICT(409, "资源冲突"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),

    INTERNAL_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),

    SESSION_CLOSED(1001, "会话已关闭"),
    AI_TIMEOUT(1002, "AI处理超时"),
    KNOWLEDGE_NOT_FOUND(1003, "知识库未命中");

    private final int code;
    private final String message;

    HttpStatusEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
}