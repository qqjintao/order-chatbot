package com.ai.examples.chatbot.controller;

import com.ai.examples.chatbot.common.Result;
import com.ai.examples.chatbot.dao.entity.ChatHistory;
import com.ai.examples.chatbot.dto.ChatRequest;
import com.ai.examples.chatbot.dto.ChatResponse;
import com.ai.examples.chatbot.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chatbot Controller", description = "智能聊天机器人接口")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/send")
    @Operation(summary = "发送消息", description = "向 Chatbot Agent 发送消息并获取 AI 回复，消息和回复均持久化到 MySQL")
    public Result<ChatResponse> sendMessage(@RequestBody ChatRequest request) {
        ChatResponse data = chatService.chat(request);
        return Result.ok(data);
    }

    @GetMapping("/history")
    @Operation(summary = "获取聊天历史", description = "根据 sessionId 获取指定会话的完整聊天记录")
    public Result<List<ChatHistory>> getHistory(
            @Parameter(description = "会话ID", required = true, example = "abc123")
            @RequestParam String sessionId) {
        List<ChatHistory> data = chatService.getHistory(sessionId);
        return Result.ok(data);
    }

    @DeleteMapping("/history")
    @Operation(summary = "清除聊天历史", description = "清除指定会话的所有聊天记录")
    public Result<Void> clearHistory(
            @Parameter(description = "会话ID", required = true, example = "abc123")
            @RequestParam String sessionId) {
        chatService.clearHistory(sessionId);
        return Result.ok();
    }
}
