package com.chatbot.module.agent.controller;

import com.chatbot.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/agent")
@Tag(name = "坐席工作台", description = "坐席状态/会话接管/转接")
public class AgentController {

    private final Map<String, String> agentStatus = new HashMap<>();
    private final List<Map<String, Object>> sessionQueue = new ArrayList<>();

    @GetMapping("/status")
    @Operation(summary = "获取坐席状态")
    public Result<Map<String, Object>> getStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "online");
        result.put("onlineTime", System.currentTimeMillis());
        return Result.ok(result);
    }

    @PutMapping("/status")
    @Operation(summary = "切换坐席状态")
    public Result<Void> updateStatus(@RequestBody Map<String, String> body) {
        return Result.ok();
    }

    @GetMapping("/queue")
    @Operation(summary = "待分配会话队列")
    public Result<List<Map<String, Object>>> queue() {
        return Result.ok(Collections.emptyList());
    }

    @PostMapping("/sessions/{sessionId}/accept")
    @Operation(summary = "接管会话")
    public Result<Void> accept(@PathVariable String sessionId) {
        return Result.ok();
    }

    @PostMapping("/sessions/{sessionId}/transfer")
    @Operation(summary = "转接会话")
    public Result<Void> transfer(@PathVariable String sessionId, @RequestBody Map<String, Object> body) {
        return Result.ok();
    }
}