package com.ai.examples.chatbot.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * 聊天响应 - 纯业务数据，不含状态码
 */
@Schema(description = "聊天响应数据")
public class ChatResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "AI 回复内容", example = "123 + 456 的结果是 579")
    private String reply;

    @Schema(description = "会话ID", example = "session-001")
    private String sessionId;

    public ChatResponse() {
    }

    public ChatResponse(String reply, String sessionId) {
        this.reply = reply;
        this.sessionId = sessionId;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String toString() {
        return "ChatResponse{" +
                "reply='" + reply + '\'' +
                ", sessionId='" + sessionId + '\'' +
                '}';
    }
}
