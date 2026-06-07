package com.chatbot.module.ticket.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chatbot.common.Result;
import com.chatbot.dao.entity.Ticket;
import com.chatbot.dao.entity.TicketRecord;
import com.chatbot.dao.mapper.TicketMapper;
import com.chatbot.dao.mapper.TicketRecordMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/tickets")
@Tag(name = "工单管理", description = "工单CRUD/流转/分配")
public class TicketController {

    private final TicketMapper ticketMapper;
    private final TicketRecordMapper ticketRecordMapper;

    public TicketController(TicketMapper ticketMapper, TicketRecordMapper ticketRecordMapper) {
        this.ticketMapper = ticketMapper;
        this.ticketRecordMapper = ticketRecordMapper;
    }

    @GetMapping
    @Operation(summary = "工单列表")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Ticket> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) wrapper.eq(Ticket::getStatus, status);
        if (priority != null && !priority.isEmpty()) wrapper.eq(Ticket::getPriority, priority);
        if (keyword != null && !keyword.isEmpty()) wrapper.like(Ticket::getTitle, keyword);
        wrapper.orderByDesc(Ticket::getCreateTime);

        Page<Ticket> pageResult = ticketMapper.selectPage(new Page<>(page, pageSize), wrapper);
        Map<String, Object> result = new HashMap<>();
        result.put("records", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("totalPages", pageResult.getPages());
        return Result.ok(result);
    }

    @PostMapping
    @Operation(summary = "创建工单")
    public Result<Ticket> create(@RequestBody Ticket ticket) {
        ticket.setTicketNo("TK" + System.currentTimeMillis());
        ticket.setStatus("PENDING");
        ticket.setCreateTime(LocalDateTime.now());
        ticket.setUpdateTime(LocalDateTime.now());
        ticketMapper.insert(ticket);

        TicketRecord record = new TicketRecord();
        record.setTicketNo(ticket.getTicketNo());
        record.setAction("创建工单");
        record.setToStatus("PENDING");
        record.setNote(ticket.getDescription());
        record.setCreateTime(LocalDateTime.now());
        ticketRecordMapper.insert(record);

        return Result.ok(ticket);
    }

    @GetMapping("/{ticketNo}")
    @Operation(summary = "工单详情")
    public Result<Map<String, Object>> detail(@PathVariable String ticketNo) {
        Ticket ticket = ticketMapper.selectById(ticketNo);
        List<TicketRecord> records = ticketRecordMapper.selectList(
                new LambdaQueryWrapper<TicketRecord>().eq(TicketRecord::getTicketNo, ticketNo).orderByAsc(TicketRecord::getCreateTime));
        Map<String, Object> result = new HashMap<>();
        result.put("ticket", ticket);
        result.put("records", records);
        return Result.ok(result);
    }

    @PutMapping("/{ticketNo}/status")
    @Operation(summary = "更新工单状态")
    public Result<Void> updateStatus(@PathVariable String ticketNo, @RequestBody Map<String, Object> body) {
        Ticket ticket = ticketMapper.selectById(ticketNo);
        if (ticket != null) {
            String oldStatus = ticket.getStatus();
            ticket.setStatus((String) body.get("status"));
            ticket.setUpdateTime(LocalDateTime.now());
            ticketMapper.updateById(ticket);

            TicketRecord record = new TicketRecord();
            record.setTicketNo(ticketNo);
            record.setAction("状态变更");
            record.setFromStatus(oldStatus);
            record.setToStatus((String) body.get("status"));
            record.setNote((String) body.getOrDefault("operatorNote", ""));
            record.setCreateTime(LocalDateTime.now());
            ticketRecordMapper.insert(record);
        }
        return Result.ok();
    }

    @PostMapping("/{ticketNo}/assign")
    @Operation(summary = "分配处理人")
    public Result<Void> assign(@PathVariable String ticketNo, @RequestBody Map<String, Object> body) {
        Ticket ticket = ticketMapper.selectById(ticketNo);
        if (ticket != null) {
            ticket.setAssigneeId(Long.valueOf(body.get("assigneeId").toString()));
            ticket.setAssigneeName((String) body.get("assigneeName"));
            ticket.setStatus("PROCESSING");
            ticket.setUpdateTime(LocalDateTime.now());
            ticketMapper.updateById(ticket);
        }
        return Result.ok();
    }

    @PostMapping("/{ticketNo}/resolve")
    @Operation(summary = "解决工单")
    public Result<Void> resolve(@PathVariable String ticketNo, @RequestBody Map<String, String> body) {
        Ticket ticket = ticketMapper.selectById(ticketNo);
        if (ticket != null) {
            ticket.setStatus("RESOLVED");
            ticket.setResolution(body.get("resolution"));
            ticket.setResolutionType(body.get("resolutionType"));
            ticket.setResolveTime(LocalDateTime.now());
            ticket.setUpdateTime(LocalDateTime.now());
            ticketMapper.updateById(ticket);
        }
        return Result.ok();
    }
}