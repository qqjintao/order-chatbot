package com.ai.examples.chatbot.service;

import com.ai.examples.chatbot.dao.entity.ChatHistory;
import com.ai.examples.chatbot.dto.ChatRequest;
import com.ai.examples.chatbot.dto.ChatResponse;

import java.util.List;

public interface ChatService {

    /**
     * 发送消息并获取 AI 回复
     */
    ChatResponse chat(ChatRequest request);

    /**
     * 获取会话历史记录
     */
    List<ChatHistory> getHistory(String sessionId);

    /**
     * 清除会话历史
     */
    void clearHistory(String sessionId);
}
