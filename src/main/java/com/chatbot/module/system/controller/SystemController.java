package com.chatbot.module.system.controller;

import com.chatbot.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/system")
@Tag(name = "系统管理", description = "配置/Prompt/用户管理")
public class SystemController {

    @GetMapping("/config")
    @Operation(summary = "获取系统配置")
    public Result<Map<String, Object>> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("systemName", "通用智能客服AI系统");
        config.put("version", "2.0.0");
        config.put("maxSessionPerAgent", 10);
        config.put("autoCloseHours", 24);
        return Result.ok(config);
    }

    @PutMapping("/config")
    @Operation(summary = "更新系统配置")
    public Result<Void> updateConfig(@RequestBody Map<String, Object> config) {
        return Result.ok();
    }

    @GetMapping("/prompts")
    @Operation(summary = "Prompt模板列表")
    public Result<List<Map<String, Object>>> prompts() {
        return Result.ok(List.of(
                Map.of("id", 1, "name", "默认客服Prompt", "type", "system"),
                Map.of("id", 2, "name", "工单创建Prompt", "type", "ticket")
        ));
    }

    @PostMapping("/prompts")
    @Operation(summary = "创建Prompt模板")
    public Result<Void> createPrompt(@RequestBody Map<String, Object> prompt) {
        return Result.ok();
    }

    @GetMapping("/users")
    @Operation(summary = "用户列表")
    public Result<List<Map<String, Object>>> users() {
        return Result.ok(List.of(
                Map.of("id", 1, "username", "admin", "realName", "管理员", "role", "admin"),
                Map.of("id", 2, "username", "agent", "realName", "客服坐席", "role", "agent")
        ));
    }

    @GetMapping("/logs")
    @Operation(summary = "操作日志")
    public Result<List<Map<String, Object>>> logs() {
        return Result.ok(Collections.emptyList());
    }
}