package com.chatbot.module.chat.service;

import com.chatbot.common.BusinessException;
import com.chatbot.common.HttpStatusEnum;
import com.chatbot.dao.entity.ChatMessage;
import com.chatbot.dao.entity.ChatSession;
import com.chatbot.dao.entity.KnowledgeDoc;
import com.chatbot.dao.mapper.ChatMessageMapper;
import com.chatbot.dao.mapper.ChatSessionMapper;
import com.chatbot.module.chat.dto.ChatRequest;
import com.chatbot.module.chat.dto.ChatResponse;
import com.chatbot.module.knowledge.service.KnowledgeService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatModel chatModel;
    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;
    private final KnowledgeService knowledgeService;
    private final LocalAiService localAiService;
    private final ObjectMapper objectMapper;

    @Value("${chatbot.rag.enabled:true}")
    private boolean ragEnabled;

    @Value("${chatbot.rag.top-k:5}")
    private int ragTopK;

    @Value("${chatbot.ai.mock-enabled:true}")
    private boolean mockEnabled;

    public ChatService(ChatModel chatModel, ChatSessionMapper sessionMapper, 
                      ChatMessageMapper messageMapper, KnowledgeService knowledgeService,
                      LocalAiService localAiService) {
        this.chatModel = chatModel;
        this.sessionMapper = sessionMapper;
        this.messageMapper = messageMapper;
        this.knowledgeService = knowledgeService;
        this.localAiService = localAiService;
        this.objectMapper = new ObjectMapper();
    }

    @Transactional
    public ChatResponse chat(ChatRequest request) {
        long startTime = System.currentTimeMillis();

        ChatSession session = getOrCreateSession(request.getSessionId(), request.getChannel());

        ChatMessage userMsg = new ChatMessage();
        userMsg.setMessageId(UUID.randomUUID().toString().replace("-", "").substring(0, 32));
        userMsg.setSessionId(session.getSessionId());
        userMsg.setRole("user");
        userMsg.setContent(request.getMessage());
        userMsg.setMessageType(request.getMessageType());
        userMsg.setCreateTime(LocalDateTime.now());
        messageMapper.insert(userMsg);

        String reply;
        String intent = "general";
        String sentiment = "neutral";
        boolean needHumanTransfer = false;
        List<KnowledgeDoc> matchedDocs = new ArrayList<>();

        try {
            List<KnowledgeDoc> knowledgeDocs = new ArrayList<>();
            
            if (ragEnabled) {
                knowledgeDocs = knowledgeService.searchWithRanking(request.getMessage(), ragTopK);
                matchedDocs.addAll(knowledgeDocs);
                log.info("RAG检索完成: 找到 {} 条相关知识", knowledgeDocs.size());
            }

            String prompt = buildPrompt(request.getMessage(), knowledgeDocs);
            
            // 如果启用mock或外部AI不可用，直接使用本地服务
            if (mockEnabled || chatModel == null) {
                log.info("使用本地AI服务响应");
                reply = localAiService.generateResponseWithKnowledge(request.getMessage(), knowledgeDocs);
            } else {
                try {
                    log.info("尝试调用外部AI服务");
                    Prompt aiPrompt = new Prompt(new UserMessage(prompt));
                    var aiResponse = chatModel.call(aiPrompt);
                    reply = aiResponse.getResult().getOutput().getText();
                    log.info("外部AI调用成功");
                } catch (Exception e) {
                    log.warn("外部AI调用失败，降级到本地服务: {}", e.getMessage());
                    reply = localAiService.generateResponseWithKnowledge(request.getMessage(), knowledgeDocs);
                }
            }
            
            intent = analyzeIntent(request.getMessage());
            sentiment = analyzeSentiment(reply);
            needHumanTransfer = shouldTransferToHuman(intent, sentiment, reply);

        } catch (Exception e) {
            log.error("AI调用失败: {}", e.getMessage());
            reply = localAiService.generateResponse(request.getMessage());
            intent = "system_error";
            needHumanTransfer = true;
        }

        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setMessageId(UUID.randomUUID().toString().replace("-", "").substring(0, 32));
        aiMsg.setSessionId(session.getSessionId());
        aiMsg.setRole("assistant");
        aiMsg.setContent(reply);
        aiMsg.setMessageType("text");
        aiMsg.setIntent(intent);
        aiMsg.setSentiment(sentiment);
        
        if (!matchedDocs.isEmpty()) {
            try {
                aiMsg.setRagDocs(objectMapper.writeValueAsString(matchedDocs));
            } catch (JsonProcessingException e) {
                log.warn("序列化RAG文档失败: {}", e.getMessage());
            }
        }
        
        aiMsg.setCreateTime(LocalDateTime.now());
        messageMapper.insert(aiMsg);

        session.setMessageCount((session.getMessageCount() == null ? 0 : session.getMessageCount()) + 2);
        session.setTitle(request.getMessage().length() > 20 ? request.getMessage().substring(0, 20) + "..." : request.getMessage());
        session.setUpdateTime(LocalDateTime.now());
        sessionMapper.updateById(session);

        double responseTime = (System.currentTimeMillis() - startTime) / 1000.0;

        ChatResponse response = new ChatResponse();
        response.setMessageId(aiMsg.getMessageId());
        response.setSessionId(session.getSessionId());
        response.setContent(reply);
        response.setIntent(intent);
        response.setSentiment(sentiment);
        response.setConfidence(0.95);
        response.setNeedHumanTransfer(needHumanTransfer);
        response.setResponseTime(responseTime);
        response.setSuggestions(getSuggestions(intent));

        return response;
    }

    private String buildPrompt(String userMessage, List<KnowledgeDoc> knowledgeDocs) {
        StringBuilder prompt = new StringBuilder();
        
        if (!knowledgeDocs.isEmpty()) {
            prompt.append("根据以下知识库内容回答用户问题：\n");
            prompt.append("========================\n");
            for (KnowledgeDoc doc : knowledgeDocs) {
                prompt.append("【").append(doc.getTitle()).append("】\n");
                prompt.append(doc.getSummary() != null ? doc.getSummary() : doc.getContent()).append("\n");
                prompt.append("------------------------\n");
            }
            prompt.append("========================\n");
        }
        
        prompt.append("用户问题：").append(userMessage).append("\n");
        prompt.append("请根据知识库内容用自然、友好的语言回答用户问题。");
        
        return prompt.toString();
    }

    private String analyzeIntent(String message) {
        message = message.toLowerCase();
        if (message.contains("订单") || message.contains("物流") || message.contains("快递")) {
            return "order_tracking";
        } else if (message.contains("退款") || message.contains("退货") || message.contains("换货")) {
            return "refund";
        } else if (message.contains("投诉") || message.contains("问题") || message.contains("建议")) {
            return "complaint";
        } else if (message.contains("人工") || message.contains("客服")) {
            return "transfer_human";
        } else if (message.contains("价格") || message.contains("优惠") || message.contains("折扣")) {
            return "pricing";
        }
        return "general";
    }

    private String analyzeSentiment(String response) {
        response = response.toLowerCase();
        if (response.contains("抱歉") || response.contains("遗憾") || response.contains("无法")) {
            return "negative";
        } else if (response.contains("高兴") || response.contains("感谢") || response.contains("欢迎")) {
            return "positive";
        }
        return "neutral";
    }

    private boolean shouldTransferToHuman(String intent, String sentiment, String reply) {
        if ("transfer_human".equals(intent)) {
            return true;
        }
        if ("negative".equals(sentiment) && (reply.contains("无法") || reply.contains("抱歉"))) {
            return true;
        }
        return false;
    }

    private List<String> getSuggestions(String intent) {
        if ("transfer_human".equals(intent)) {
            return Arrays.asList("已为您转接人工客服", "等待坐席接入...");
        }
        return Arrays.asList("还有其他问题吗？", "联系人工客服", "查看常见问题");
    }

    public List<ChatMessage> getMessages(String sessionId) {
        return messageMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getSessionId, sessionId)
                        .orderByAsc(ChatMessage::getCreateTime));
    }

    private ChatSession getOrCreateSession(String sessionId, String channel) {
        if (sessionId != null && !sessionId.isEmpty()) {
            ChatSession session = sessionMapper.selectById(sessionId);
            if (session != null) return session;
        }
        ChatSession session = new ChatSession();
        session.setSessionId(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        session.setChannel(channel != null ? channel : "web");
        session.setStatus(1);
        session.setMessageCount(0);
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        sessionMapper.insert(session);
        return session;
    }
}