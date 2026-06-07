package com.chatbot.module.session.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chatbot.common.Result;
import com.chatbot.dao.entity.ChatMessage;
import com.chatbot.dao.entity.ChatSession;
import com.chatbot.dao.mapper.ChatMessageMapper;
import com.chatbot.dao.mapper.ChatSessionMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sessions")
@Tag(name = "会话管理", description = "会话列表/详情/关闭/转人工")
public class SessionController {

    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;

    public SessionController(ChatSessionMapper sessionMapper, ChatMessageMapper messageMapper) {
        this.sessionMapper = sessionMapper;
        this.messageMapper = messageMapper;
    }

    @GetMapping
    @Operation(summary = "会话列表")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();
        if (status != null) wrapper.eq(ChatSession::getStatus, status);
        if (channel != null && !channel.isEmpty()) wrapper.eq(ChatSession::getChannel, channel);
        if (keyword != null && !keyword.isEmpty()) wrapper.like(ChatSession::getUserName, keyword);
        wrapper.orderByDesc(ChatSession::getUpdateTime);

        Page<ChatSession> pageResult = sessionMapper.selectPage(new Page<>(page, pageSize), wrapper);
        Map<String, Object> result = new HashMap<>();
        result.put("records", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("totalPages", pageResult.getPages());
        return Result.ok(result);
    }

    @GetMapping("/{sessionId}")
    @Operation(summary = "会话详情")
    public Result<ChatSession> detail(@PathVariable String sessionId) {
        return Result.ok(sessionMapper.selectById(sessionId));
    }

    @GetMapping("/{sessionId}/messages")
    @Operation(summary = "消息历史")
    public Result<Map<String, Object>> messages(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int pageSize) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId).orderByAsc(ChatMessage::getCreateTime);
        Page<ChatMessage> pageResult = messageMapper.selectPage(new Page<>(page, pageSize), wrapper);
        Map<String, Object> result = new HashMap<>();
        result.put("records", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("totalPages", pageResult.getPages());
        return Result.ok(result);
    }

    @PostMapping
    @Operation(summary = "创建会话")
    public Result<ChatSession> create(@RequestBody Map<String, String> body) {
        ChatSession session = new ChatSession();
        session.setSessionId(java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        session.setChannel(body.getOrDefault("channel", "web"));
        session.setStatus(1);
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        sessionMapper.insert(session);
        return Result.ok(session);
    }

    @PutMapping("/{sessionId}/close")
    @Operation(summary = "关闭会话")
    public Result<Void> close(@PathVariable String sessionId, @RequestBody Map<String, String> body) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session != null) {
            session.setStatus(2);
            session.setCloseReason(body.getOrDefault("closeReason", "问题已解决"));
            session.setUpdateTime(LocalDateTime.now());
            sessionMapper.updateById(session);
        }
        return Result.ok();
    }

    @PostMapping("/{sessionId}/transfer")
    @Operation(summary = "转人工")
    public Result<Void> transfer(@PathVariable String sessionId, @RequestBody Map<String, Object> body) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session != null) {
            session.setStatus(3);
            if (body.get("agentId") != null) {
                session.setAgentId(Long.valueOf(body.get("agentId").toString()));
            }
            session.setUpdateTime(LocalDateTime.now());
            sessionMapper.updateById(session);
        }
        return Result.ok();
    }
}