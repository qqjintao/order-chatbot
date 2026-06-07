package com.chatbot.module.analytics.controller;

import com.chatbot.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "数据分析", description = "运营看板/趋势/热点")
public class AnalyticsController {

    @GetMapping("/dashboard")
    @Operation(summary = "仪表盘数据")
    public Result<Map<String, Object>> dashboard() {
        Map<String, Object> result = new HashMap<>();

        Map<String, Object> todayStats = new HashMap<>();
        todayStats.put("totalSessions", 156);
        todayStats.put("aiHandledRate", 82.5);
        todayStats.put("avgResponseTime", 2.8);
        todayStats.put("pendingTickets", 5);
        todayStats.put("satisfactionRate", 94.2);
        result.put("todayStats", todayStats);

        List<Map<String, Object>> sessionTrend = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            Map<String, Object> point = new HashMap<>();
            point.put("date", "06-0" + i);
            point.put("count", 100 + new Random().nextInt(100));
            sessionTrend.add(point);
        }
        result.put("sessionTrend", sessionTrend);

        List<Map<String, Object>> channelDistribution = List.of(
                Map.of("channel", "Web", "count", 68, "percentage", 43.6),
                Map.of("channel", "H5", "count", 45, "percentage", 28.8),
                Map.of("channel", "API", "count", 28, "percentage", 17.9),
                Map.of("channel", "企业微信", "count", 15, "percentage", 9.6)
        );
        result.put("channelDistribution", channelDistribution);

        List<Map<String, Object>> hotTopics = List.of(
                Map.of("topic", "物流查询", "count", 45),
                Map.of("topic", "退换货流程", "count", 32),
                Map.of("topic", "商品咨询", "count", 28)
        );
        result.put("hotTopics", hotTopics);

        List<Map<String, Object>> intentDistribution = List.of(
                Map.of("intent", "order_query", "count", 52, "percentage", 33.3),
                Map.of("intent", "faq", "count", 48, "percentage", 30.8),
                Map.of("intent", "complaint", "count", 25, "percentage", 16.0),
                Map.of("intent", "other", "count", 31, "percentage", 19.9)
        );
        result.put("intentDistribution", intentDistribution);

        return Result.ok(result);
    }

    @GetMapping("/trend")
    @Operation(summary = "趋势数据")
    public Result<List<Map<String, Object>>> trend(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "day") String granularity) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            result.add(Map.of("date", "2026-06-0" + (i + 1), "count", 100 + new Random().nextInt(100)));
        }
        return Result.ok(result);
    }

    @GetMapping("/hot-topics")
    @Operation(summary = "热点问题")
    public Result<List<Map<String, Object>>> hotTopics(
            @RequestParam(defaultValue = "20") int limit) {
        return Result.ok(List.of(
                Map.of("topic", "物流查询", "count", 45),
                Map.of("topic", "退换货流程", "count", 32)
        ));
    }

    @PostMapping("/report")
    @Operation(summary = "生成报表")
    public Result<Map<String, Object>> report(@RequestBody Map<String, String> body) {
        return Result.ok(Map.of("reportUrl", "/reports/weekly-2026-06-07.pdf"));
    }
}