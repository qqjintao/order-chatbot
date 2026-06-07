package com.chatbot.module.chat.controller;

import com.chatbot.common.Result;
import com.chatbot.dao.entity.ChatMessage;
import com.chatbot.module.chat.dto.ChatRequest;
import com.chatbot.module.chat.dto.ChatResponse;
import com.chatbot.module.chat.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat")
@Tag(name = "智能对话", description = "聊天/流式对话/评价")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/send")
    @Operation(summary = "发送消息（普通模式）")
    public Result<ChatResponse> sendMessage(@Valid @RequestBody ChatRequest request) {
        return Result.ok(chatService.chat(request));
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式对话（SSE）")
    public SseEmitter streamMessage(@Valid @RequestBody ChatRequest request) {
        SseEmitter emitter = new SseEmitter(300000L);
        try {
            String requestId = UUID.randomUUID().toString().substring(0, 8);
            emitter.send(SseEmitter.event().name("start").data(Map.of("type", "start", "requestId", requestId)));

            ChatResponse response = chatService.chat(request);
            String content = response.getContent();

            // 模拟流式输出
            int chunkSize = 5;
            for (int i = 0; i < content.length(); i += chunkSize) {
                int end = Math.min(i + chunkSize, content.length());
                emitter.send(SseEmitter.event().name("content")
                        .data(Map.of("type", "content", "content", content.substring(i, end))));
                Thread.sleep(30);
            }

            emitter.send(SseEmitter.event().name("suggestions")
                    .data(Map.of("type", "suggestions", "suggestions", response.getSuggestions())));
            emitter.send(SseEmitter.event().name("done")
                    .data(Map.of("type", "done", "tokensUsed", response.getTokensUsed() != null ? response.getTokensUsed() : 0, "responseTime", response.getResponseTime())));
            emitter.complete();
        } catch (IOException e) {
            emitter.completeWithError(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            emitter.completeWithError(e);
        }
        return emitter;
    }

    @PostMapping("/{messageId}/feedback")
    @Operation(summary = "消息评价")
    public Result<Void> feedback(@PathVariable String messageId, @RequestBody Map<String, Object> body) {
        return Result.ok();
    }

    @GetMapping("/quick-replies")
    @Operation(summary = "获取快捷回复")
    public Result<List<String>> getQuickReplies() {
        return Result.ok(List.of("如何查询进度？", "如何联系人工客服？", "常见问题有哪些？", "服务时间是什么？"));
    }
}