package com.chatbot.module.chat.dto;

import java.util.List;
import java.util.Map;

public class ChatResponse {

    private String messageId;
    private String sessionId;
    private String content;
    private String intent;
    private String sentiment;
    private Double confidence;
    private List<Map<String, Object>> ragDocs;
    private List<String> suggestions;
    private boolean needHumanTransfer;
    private Integer tokensUsed;
    private Double responseTime;

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }
    public String getSentiment() { return sentiment; }
    public void setSentiment(String sentiment) { this.sentiment = sentiment; }
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
    public List<Map<String, Object>> getRagDocs() { return ragDocs; }
    public void setRagDocs(List<Map<String, Object>> ragDocs) { this.ragDocs = ragDocs; }
    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }
    public boolean isNeedHumanTransfer() { return needHumanTransfer; }
    public void setNeedHumanTransfer(boolean needHumanTransfer) { this.needHumanTransfer = needHumanTransfer; }
    public Integer getTokensUsed() { return tokensUsed; }
    public void setTokensUsed(Integer tokensUsed) { this.tokensUsed = tokensUsed; }
    public Double getResponseTime() { return responseTime; }
    public void setResponseTime(Double responseTime) { this.responseTime = responseTime; }
}