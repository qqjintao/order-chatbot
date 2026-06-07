package com.chatbot.module.knowledge.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 文件解析服务
 * 支持解析 PDF、Word (DOC/DOCX)、Excel (XLS/XLSX)、TXT、Markdown 等格式
 */
@Slf4j
@Service
public class FileParserService {

    // 支持的文件类型映射
    private static final Map<String, String> FILE_TYPE_MAP = new HashMap<>();
    static {
        FILE_TYPE_MAP.put("pdf", "PDF文档");
        FILE_TYPE_MAP.put("doc", "Word文档");
        FILE_TYPE_MAP.put("docx", "Word文档");
        FILE_TYPE_MAP.put("xls", "Excel表格");
        FILE_TYPE_MAP.put("xlsx", "Excel表格");
        FILE_TYPE_MAP.put("txt", "文本文档");
        FILE_TYPE_MAP.put("md", "Markdown文档");
        FILE_TYPE_MAP.put("csv", "CSV文件");
        FILE_TYPE_MAP.put("json", "JSON文件");
    }

    /**
     * 解析文件内容
     * @param file 上传的文件
     * @return 解析后的文本内容
     * @throws IOException 如果解析失败
     */
    public String parseContent(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IOException("文件名为空");
        }

        String extension = getFileExtension(filename).toLowerCase();
        log.info("开始解析文件: {}, 扩展名: {}", filename, extension);

        String content = switch (extension) {
            case "pdf" -> parsePdf(file.getInputStream());
            case "docx" -> parseDocx(file.getInputStream());
            case "doc" -> parseDoc(file.getInputStream());
            case "xlsx" -> parseXlsx(file.getInputStream());
            case "xls" -> parseXls(file.getInputStream());
            case "txt", "md", "csv", "json" -> parseText(file);
            default -> throw new IOException("不支持的文件格式: " + extension);
        };

        // 清理和规范化内容
        content = normalizeContent(content);
        log.info("文件解析完成: {}, 内容长度: {} 字符", filename, content.length());
        return content;
    }

    /**
     * 获取文件类型描述
     */
    public String getFileTypeDescription(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        return FILE_TYPE_MAP.getOrDefault(extension, "未知格式");
    }

    /**
     * 判断是否为支持的格式
     */
    public boolean isSupported(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        return FILE_TYPE_MAP.containsKey(extension);
    }

    /**
     * 从File对象解析内容（用于重建索引）
     */
    public String parseContentFromFile(File file) throws IOException {
        String filename = file.getName();
        String extension = getFileExtension(filename).toLowerCase();
        log.info("从文件解析内容: {}, 扩展名: {}", filename, extension);

        String content;
        try (FileInputStream fis = new FileInputStream(file)) {
            content = switch (extension) {
                case "pdf" -> parsePdf(fis);
                case "docx" -> parseDocx(fis);
                case "doc" -> parseDoc(fis);
                case "xlsx" -> parseXlsx(fis);
                case "xls" -> parseXls(fis);
                case "txt", "md", "csv", "json" -> new String(java.nio.file.Files.readAllBytes(file.toPath()), "UTF-8");
                default -> throw new IOException("不支持的文件格式: " + extension);
            };
        }

        content = normalizeContent(content);
        log.info("文件解析完成: {}, 内容长度: {} 字符", filename, content.length());
        return content;
    }

    /**
     * 解析PDF文件
     */
    private String parsePdf(InputStream inputStream) throws IOException {
        try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        } catch (Exception e) {
            log.error("PDF解析失败: {}", e.getMessage());
            throw new IOException("PDF解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析DOCX文件 (Office 2007+)
     */
    private String parseDocx(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        } catch (Exception e) {
            log.error("DOCX解析失败: {}", e.getMessage());
            throw new IOException("DOCX解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析DOC文件 (Office 2003) - 简化实现
     */
    private String parseDoc(InputStream inputStream) throws IOException {
        try {
            // DOC格式需要额外依赖，这里简单处理
            byte[] bytes = inputStream.readAllBytes();
            // 尝试提取可读文本（简化实现）
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length - 1; i++) {
                char c = (char) bytes[i];
                if (c >= 32 && c <= 126 || Character.isLetter((char) bytes[i + 1])) {
                    if (Character.isLetterOrDigit(c) || Character.isWhitespace(c) || c == '.' || c == ',' || c == '!' || c == '?') {
                        sb.append(c);
                    }
                }
            }
            return sb.toString().trim();
        } catch (Exception e) {
            log.error("DOC解析失败: {}", e.getMessage());
            throw new IOException("DOC格式暂不支持，请转换为DOCX或PDF格式上传", e);
        }
    }

    /**
     * 解析XLSX文件 (Excel 2007+)
     */
    private String parseXlsx(InputStream inputStream) throws IOException {
        StringBuilder content = new StringBuilder();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                String sheetName = workbook.getSheetName(sheetIndex);
                content.append("【工作表: ").append(sheetName).append("】\n");

                for (Row row : sheet) {
                    StringBuilder rowContent = new StringBuilder();
                    for (Cell cell : row) {
                        String cellValue = getCellValueAsString(cell);
                        if (!cellValue.isEmpty()) {
                            rowContent.append(cellValue).append(" | ");
                        }
                    }
                    if (rowContent.length() > 0) {
                        content.append(rowContent).append("\n");
                    }
                }
                content.append("\n");
            }
        } catch (Exception e) {
            log.error("XLSX解析失败: {}", e.getMessage());
            throw new IOException("Excel解析失败: " + e.getMessage(), e);
        }
        return content.toString();
    }

    /**
     * 解析XLS文件 (Excel 2003)
     */
    private String parseXls(InputStream inputStream) throws IOException {
        StringBuilder content = new StringBuilder();
        try (Workbook workbook = new HSSFWorkbook(inputStream)) {
            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                String sheetName = workbook.getSheetName(sheetIndex);
                content.append("【工作表: ").append(sheetName).append("】\n");

                for (Row row : sheet) {
                    StringBuilder rowContent = new StringBuilder();
                    for (Cell cell : row) {
                        String cellValue = getCellValueAsString(cell);
                        if (!cellValue.isEmpty()) {
                            rowContent.append(cellValue).append(" | ");
                        }
                    }
                    if (rowContent.length() > 0) {
                        content.append(rowContent).append("\n");
                    }
                }
                content.append("\n");
            }
        } catch (Exception e) {
            log.error("XLS解析失败: {}", e.getMessage());
            throw new IOException("Excel解析失败: " + e.getMessage(), e);
        }
        return content.toString();
    }

    /**
     * 解析文本文件
     */
    private String parseText(MultipartFile file) throws IOException {
        return new String(file.getBytes(), "UTF-8");
    }

    /**
     * 获取单元格的值作为字符串
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toString();
                } else {
                    double numValue = cell.getNumericCellValue();
                    if (numValue == Math.floor(numValue)) {
                        yield String.valueOf((long) numValue);
                    } else {
                        yield String.valueOf(numValue);
                    }
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue();
                } catch (Exception e) {
                    yield String.valueOf(cell.getNumericCellValue());
                }
            }
            default -> "";
        };
    }

    /**
     * 规范化文本内容
     */
    private String normalizeContent(String content) {
        if (content == null) {
            return "";
        }
        // 替换多个空白字符为单个空格
        content = content.replaceAll("\\s+", " ");
        // 移除特殊控制字符
        content = content.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "");
        // 规范化换行符
        content = content.replaceAll("\r\n", "\n").replaceAll("\r", "\n");
        // 移除连续的空行
        content = content.replaceAll("\n{3,}", "\n\n");
        return content.trim();
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * 将内容分割成多个块（用于向量存储）
     */
    public List<String> splitIntoChunks(String content, int maxChunkSize, int overlap) {
        if (content == null || content.isEmpty()) {
            return List.of();
        }
        if (content.length() <= maxChunkSize) {
            return List.of(content);
        }

        String[] paragraphs = content.split("\n\n");
        StringBuilder currentChunk = new StringBuilder();
        java.util.List<String> chunks = new java.util.ArrayList<>();

        for (String paragraph : paragraphs) {
            if (currentChunk.length() + paragraph.length() + 2 <= maxChunkSize) {
                if (currentChunk.length() > 0) {
                    currentChunk.append("\n\n");
                }
                currentChunk.append(paragraph);
            } else {
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString().trim());
                    // 添加重叠部分
                    String overlapText = currentChunk.toString();
                    int overlapStart = Math.max(0, overlapText.length() - overlap);
                    currentChunk = new StringBuilder(overlapText.substring(overlapStart));
                    if (currentChunk.length() > 0 && !currentChunk.toString().endsWith("\n\n")) {
                        currentChunk.append("\n\n");
                    }
                }
                currentChunk.append(paragraph);
            }
        }

        // 添加最后一个块
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }
}
