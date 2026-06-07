package com.chatbot.module.knowledge.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chatbot.common.Result;
import com.chatbot.dao.entity.KnowledgeCategory;
import com.chatbot.dao.entity.KnowledgeDoc;
import com.chatbot.dao.mapper.KnowledgeCategoryMapper;
import com.chatbot.dao.mapper.KnowledgeDocMapper;
import com.chatbot.module.knowledge.service.KnowledgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/knowledge")
@Tag(name = "知识库管理", description = "知识库CRUD/分类/上传/搜索")
public class KnowledgeController {

    private final KnowledgeDocMapper docMapper;
    private final KnowledgeCategoryMapper categoryMapper;
    private final KnowledgeService knowledgeService;

    public KnowledgeController(KnowledgeDocMapper docMapper, 
                              KnowledgeCategoryMapper categoryMapper,
                              KnowledgeService knowledgeService) {
        this.docMapper = docMapper;
        this.categoryMapper = categoryMapper;
        this.knowledgeService = knowledgeService;
    }

    @GetMapping
    @Operation(summary = "知识库文档列表")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        
        // 使用 QueryWrapper 构建查询条件，避免 Lambda 参数传递问题
        QueryWrapper<KnowledgeDoc> wrapper = new QueryWrapper<>();
        if (categoryId != null) {
            wrapper.eq("category_id", categoryId);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            // 直接使用字符串方式构建 or 条件，避免 lambda 参数问题
            wrapper.like("title", keyword).or().like("content", keyword);
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq("category_name", category);
        }
        wrapper.orderByDesc("update_time");

        Page<KnowledgeDoc> pageResult = docMapper.selectPage(new Page<>(page, pageSize), wrapper);
        Map<String, Object> result = new HashMap<>();
        result.put("records", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("totalPages", pageResult.getPages());
        return Result.ok(result);
    }

    @GetMapping("/{docId}")
    @Operation(summary = "获取文档详情")
    public Result<KnowledgeDoc> getById(@PathVariable String docId) {
        KnowledgeDoc doc = knowledgeService.getById(docId);
        return Result.ok(doc);
    }

    @PostMapping("/upload")
    @Operation(summary = "上传文档（支持表单绑定）")
    public Result<Map<String, Object>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String tags) {
        
        KnowledgeDoc doc = knowledgeService.upload(file, categoryId, title, tags);
        
        Map<String, Object> result = new HashMap<>();
        result.put("docId", doc.getDocId());
        result.put("title", doc.getTitle());
        result.put("chunkCount", doc.getChunkCount());
        result.put("categoryId", doc.getCategoryId());
        result.put("categoryName", doc.getCategoryName());
        return Result.ok(result);
    }

    @PostMapping("/manual")
    @Operation(summary = "手动录入知识")
    public Result<KnowledgeDoc> create(@RequestBody KnowledgeDoc doc) {
        KnowledgeDoc created = knowledgeService.create(doc);
        return Result.ok(created);
    }

    @PutMapping("/{docId}")
    @Operation(summary = "更新知识")
    public Result<Void> update(@PathVariable String docId, @RequestBody KnowledgeDoc doc) {
        knowledgeService.update(docId, doc);
        return Result.ok();
    }

    @DeleteMapping("/{docId}")
    @Operation(summary = "删除知识")
    public Result<Void> delete(@PathVariable String docId) {
        knowledgeService.delete(docId);
        return Result.ok();
    }

    @PutMapping("/{docId}/reindex")
    @Operation(summary = "重建索引")
    public Result<Void> reindex(@PathVariable String docId) {
        knowledgeService.reindex(docId);
        return Result.ok();
    }

    @GetMapping("/categories")
    @Operation(summary = "获取分类树")
    public Result<List<Map<String, Object>>> categories() {
        List<KnowledgeCategory> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<KnowledgeCategory>().orderByAsc(KnowledgeCategory::getSort));
        List<Map<String, Object>> result = new ArrayList<>();
        for (KnowledgeCategory cat : categories) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", cat.getId());
            map.put("name", cat.getName());
            map.put("parentId", cat.getParentId());
            result.add(map);
        }
        return Result.ok(result);
    }

    @GetMapping("/search")
    @Operation(summary = "搜索知识库")
    public Result<List<KnowledgeDoc>> search(@RequestParam String keyword) {
        List<KnowledgeDoc> docs = knowledgeService.search(keyword);
        return Result.ok(docs);
    }

    @GetMapping("/search/rag")
    @Operation(summary = "RAG检索（用于AI客服）")
    public Result<List<Map<String, Object>>> searchRAG(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int topK) {
        List<KnowledgeDoc> docs = knowledgeService.searchWithRanking(query, topK);
        List<Map<String, Object>> result = new ArrayList<>();
        for (KnowledgeDoc doc : docs) {
            Map<String, Object> item = new HashMap<>();
            item.put("docId", doc.getDocId());
            item.put("title", doc.getTitle());
            item.put("summary", doc.getSummary());
            item.put("categoryName", doc.getCategoryName());
            result.add(item);
        }
        return Result.ok(result);
    }
}
