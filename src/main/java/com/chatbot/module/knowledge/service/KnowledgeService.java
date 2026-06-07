package com.chatbot.module.knowledge.service;

import com.chatbot.common.BusinessException;
import com.chatbot.common.HttpStatusEnum;
import com.chatbot.dao.entity.KnowledgeCategory;
import com.chatbot.dao.entity.KnowledgeDoc;
import com.chatbot.dao.mapper.KnowledgeCategoryMapper;
import com.chatbot.dao.mapper.KnowledgeDocMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class KnowledgeService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeService.class);

    private final KnowledgeDocMapper docMapper;
    private final KnowledgeCategoryMapper categoryMapper;
    private final FileParserService fileParserService;

    @Value("${chatbot.knowledge.upload-path:./uploads/knowledge}")
    private String uploadPath;

    @Value("${chatbot.rag.chunk-size:1000}")
    private int chunkSize;

    @Value("${chatbot.rag.chunk-overlap:100}")
    private int chunkOverlap;

    public KnowledgeService(KnowledgeDocMapper docMapper,
                           KnowledgeCategoryMapper categoryMapper,
                           FileParserService fileParserService) {
        this.docMapper = docMapper;
        this.categoryMapper = categoryMapper;
        this.fileParserService = fileParserService;
    }

    /**
     * 搜索知识库文档
     */
    public List<KnowledgeDoc> search(String keyword) {
        log.debug("搜索知识库: keyword={}", keyword);
        return docMapper.searchByKeyword(keyword);
    }

    /**
     * 带排名的搜索（用于RAG检索）
     */
    public List<KnowledgeDoc> searchWithRanking(String keyword, int topK) {
        log.debug("RAG检索: keyword={}, topK={}", keyword, topK);
        List<KnowledgeDoc> docs = docMapper.searchByKeyword(keyword);
        log.info("RAG检索结果: 找到 {} 条文档", docs.size());
        if (docs.size() > topK) {
            return docs.subList(0, topK);
        }
        return docs;
    }

    /**
     * 上传文档并解析内容（支持表单绑定）
     */
    @Transactional
    public KnowledgeDoc upload(MultipartFile file, Long categoryId, String title, String tags) {
        try {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                throw new BusinessException(HttpStatusEnum.BAD_REQUEST, "文件名为空");
            }

            // 检查文件类型是否支持
            if (!fileParserService.isSupported(originalFilename)) {
                throw new BusinessException(HttpStatusEnum.BAD_REQUEST, 
                    "不支持的文件格式，支持的格式：PDF、Word(DOC/DOCX)、Excel(XLS/XLSX)、TXT、Markdown等");
            }

            // 创建上传目录
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 保存原文件
            String fileName = UUID.randomUUID().toString() + "_" + originalFilename;
            Path filePath = uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);

            // 解析文件内容
            String content;
            try {
                content = fileParserService.parseContent(file);
            } catch (IOException e) {
                log.warn("文件解析失败，尝试直接读取: {}", e.getMessage());
                try {
                    content = Files.readString(filePath);
                } catch (Exception ex) {
                    throw new BusinessException(HttpStatusEnum.INTERNAL_ERROR, 
                        "文件内容读取失败: " + e.getMessage());
                }
            }

            // 生成文档摘要
            String summary = generateSummary(content);

            // 分割内容块
            List<String> chunks = fileParserService.splitIntoChunks(content, chunkSize, chunkOverlap);
            int chunkCount = chunks.size();

            // 创建知识库文档
            KnowledgeDoc doc = new KnowledgeDoc();
            doc.setDocId(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
            
            // 使用用户提供的标题，否则从文件名提取
            doc.setTitle(StringUtils.hasText(title) ? title : extractTitle(originalFilename));
            doc.setContent(content);
            doc.setSummary(summary);
            doc.setFileFormat(getFileExtension(originalFilename));
            doc.setFileUrl(filePath.toString());
            doc.setCategoryId(categoryId);
            doc.setChunkCount(chunkCount);
            doc.setTags(tags);

            // 获取分类名称
            if (categoryId != null) {
                KnowledgeCategory category = categoryMapper.selectById(categoryId);
                if (category != null) {
                    doc.setCategoryName(category.getName());
                }
            }

            doc.setStatus(1);
            doc.setCreateTime(LocalDateTime.now());
            doc.setUpdateTime(LocalDateTime.now());
            
            docMapper.insert(doc);
            log.info("文档上传成功: docId={}, title={}, chunks={}", doc.getDocId(), doc.getTitle(), chunkCount);
            return doc;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("文档上传失败: {}", e.getMessage(), e);
            throw new BusinessException(HttpStatusEnum.INTERNAL_ERROR, "文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 兼容旧版本的上传方法
     */
    @Transactional
    public KnowledgeDoc upload(MultipartFile file, Long categoryId) {
        return upload(file, categoryId, null, null);
    }

    /**
     * 手动创建知识库文档
     */
    @Transactional
    public KnowledgeDoc create(KnowledgeDoc doc) {
        doc.setDocId(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        doc.setFileFormat("manual");
        doc.setStatus(1);
        doc.setCreateTime(LocalDateTime.now());
        doc.setUpdateTime(LocalDateTime.now());
        
        // 生成摘要
        if (doc.getSummary() == null || doc.getSummary().isEmpty()) {
            doc.setSummary(generateSummary(doc.getContent()));
        }
        
        // 分割内容块
        if (doc.getContent() != null && !doc.getContent().isEmpty()) {
            List<String> chunks = fileParserService.splitIntoChunks(doc.getContent(), chunkSize, chunkOverlap);
            doc.setChunkCount(chunks.size());
        } else {
            doc.setChunkCount(1);
        }
        
        if (doc.getCategoryId() != null) {
            KnowledgeCategory category = categoryMapper.selectById(doc.getCategoryId());
            if (category != null) {
                doc.setCategoryName(category.getName());
            }
        }
        
        docMapper.insert(doc);
        log.info("手动创建文档: docId={}, title={}", doc.getDocId(), doc.getTitle());
        return doc;
    }

    /**
     * 更新文档
     */
    @Transactional
    public void update(String docId, KnowledgeDoc doc) {
        KnowledgeDoc existing = docMapper.selectById(docId);
        if (existing == null) {
            throw new BusinessException(HttpStatusEnum.NOT_FOUND, "文档不存在");
        }
        
        doc.setDocId(docId);
        doc.setUpdateTime(LocalDateTime.now());
        
        // 重新生成摘要
        if (doc.getContent() != null && !doc.getContent().isEmpty()) {
            doc.setSummary(generateSummary(doc.getContent()));
            List<String> chunks = fileParserService.splitIntoChunks(doc.getContent(), chunkSize, chunkOverlap);
            doc.setChunkCount(chunks.size());
        }
        
        docMapper.updateById(doc);
        log.info("文档更新成功: docId={}", docId);
    }

    /**
     * 删除文档
     */
    @Transactional
    public void delete(String docId) {
        KnowledgeDoc doc = docMapper.selectById(docId);
        if (doc != null && doc.getFileUrl() != null) {
            try {
                Files.deleteIfExists(Paths.get(doc.getFileUrl()));
            } catch (IOException e) {
                log.warn("删除文件失败: {}", e.getMessage());
            }
        }
        docMapper.deleteById(docId);
        log.info("文档删除成功: docId={}", docId);
    }

    /**
     * 获取文档详情
     */
    public KnowledgeDoc getById(String docId) {
        KnowledgeDoc doc = docMapper.selectById(docId);
        if (doc == null) {
            throw new BusinessException(HttpStatusEnum.NOT_FOUND, "文档不存在");
        }
        return doc;
    }

    /**
     * 重建索引
     */
    @Transactional
    public void reindex(String docId) {
        KnowledgeDoc doc = docMapper.selectById(docId);
        if (doc == null) {
            throw new BusinessException(HttpStatusEnum.NOT_FOUND, "文档不存在");
        }
        
        // 如果是上传的文件，重新解析内容
        if (doc.getFileUrl() != null && !"manual".equals(doc.getFileFormat())) {
            try {
                Path filePath = Paths.get(doc.getFileUrl());
                if (Files.exists(filePath)) {
                    String content = fileParserService.parseContentFromFile(filePath.toFile());
                    doc.setContent(content);
                    doc.setSummary(generateSummary(content));
                    List<String> chunks = fileParserService.splitIntoChunks(content, chunkSize, chunkOverlap);
                    doc.setChunkCount(chunks.size());
                    doc.setUpdateTime(LocalDateTime.now());
                    docMapper.updateById(doc);
                    log.info("文档重建索引成功: docId={}", docId);
                }
            } catch (IOException e) {
                log.error("重建索引失败: {}", e.getMessage());
                throw new BusinessException(HttpStatusEnum.INTERNAL_ERROR, "重建索引失败: " + e.getMessage());
            }
        } else {
            log.info("手动文档无需重建索引: docId={}", docId);
        }
    }

    /**
     * 分页查询文档列表
     */
    public List<KnowledgeDoc> list(int page, int pageSize, String keyword, String category) {
        int offset = (page - 1) * pageSize;
        return docMapper.selectByPage(offset, pageSize, keyword, category);
    }

    /**
     * 统计文档总数
     */
    public int count(String keyword, String category) {
        return docMapper.countByCondition(keyword, category);
    }

    /**
     * 生成文档摘要
     */
    private String generateSummary(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        // 取前300个字符作为摘要
        if (content.length() > 300) {
            return content.substring(0, 300) + "...";
        }
        return content;
    }

    /**
     * 从文件名提取标题
     */
    private String extractTitle(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "未命名文档";
        }
        // 移除扩展名
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            return filename.substring(0, lastDot);
        }
        return filename;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "unknown";
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
