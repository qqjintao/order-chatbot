package com.ai.examples.chatbot.service.impl;

import cn.hutool.core.util.StrUtil;
import com.ai.examples.chatbot.common.BusinessException;
import com.ai.examples.chatbot.common.HttpStatusEnum;
import com.ai.examples.chatbot.dao.entity.ChatHistory;
import com.ai.examples.chatbot.dao.mapper.ChatHistoryMapper;
import com.ai.examples.chatbot.dto.ChatRequest;
import com.ai.examples.chatbot.dto.ChatResponse;
import com.ai.examples.chatbot.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ChatServiceImpl implements ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);

    @Autowired
    private ChatModel chatModel;

    @Autowired
    private ChatHistoryMapper chatHistoryMapper;

    @Override
    @Transactional
    public ChatResponse chat(ChatRequest request) {
        // 参数校验
        if (StrUtil.isBlank(request.getMessage())) {
            throw new BusinessException(HttpStatusEnum.BAD_REQUEST, "消息不能为空");
        }

        // 生成或使用传入的 sessionId
        String sessionId = StrUtil.isNotBlank(request.getSessionId())
                ? request.getSessionId()
                : UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        log.info("Chat request - sessionId: {}, message: {}", sessionId, request.getMessage());

        // 1. 保存用户消息
        saveHistory(sessionId, "user", request.getMessage());

        // 2. 调用 AI
        String reply;
        try {
            Prompt prompt = new Prompt(new UserMessage(request.getMessage()));
            org.springframework.ai.chat.model.ChatResponse aiResponse = chatModel.call(prompt);
            reply = aiResponse.getResult().getOutput().getText();
            log.info("AI reply - sessionId: {}, reply: {}", sessionId, reply);
        } catch (Exception e) {
            log.error("AI call failed for sessionId: {}", sessionId, e);
            throw new BusinessException(HttpStatusEnum.SERVICE_UNAVAILABLE, "AI 服务暂时不可用，请稍后再试");
        }

        // 3. 保存 AI 回复
        saveHistory(sessionId, "assistant", reply);

        return new ChatResponse(reply, sessionId);
    }

    @Override
    public List<ChatHistory> getHistory(String sessionId) {
        return chatHistoryMapper.findBySessionIdOrderByCreateTimeAsc(sessionId);
    }

    @Override
    @Transactional
    public void clearHistory(String sessionId) {
        chatHistoryMapper.deleteBySessionId(sessionId);
        log.info("Cleared chat history for session: {}", sessionId);
    }

    private void saveHistory(String sessionId, String role, String content) {
        ChatHistory history = new ChatHistory(sessionId, role, content);
        chatHistoryMapper.insert(history);
    }
}
