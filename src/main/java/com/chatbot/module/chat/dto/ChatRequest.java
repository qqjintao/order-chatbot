package com.chatbot.module.chat.dto;

import jakarta.validation.constraints.NotBlank;

public class ChatRequest {

    private String sessionId;

    @NotBlank(message = "消息不能为空")
    private String message;

    private String channel = "web";
    private String messageType = "text";

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
}