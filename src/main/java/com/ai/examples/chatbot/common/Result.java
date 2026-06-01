package com.ai.examples.chatbot.common;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.Date;

/**
 * 统一响应体，前后端分离规范出参
 * <pre>
 * {
 *   "code": 200,
 *   "message": "success",
 *   "data": { ... },
 *   "timestamp": "2025-06-01 12:00:00"
 * }
 * </pre>
 */
@Schema(description = "统一响应体")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "状态码", example = "200")
    private Integer code;

    @Schema(description = "响应消息", example = "success")
    private String message;

    @Schema(description = "响应数据")
    private T data;

    @Schema(description = "时间戳", example = "2025-06-01 12:00:00")
    private String timestamp;

    private Result() {
    }

    // ==================== 成功 ====================

    public static <T> Result<T> ok() {
        return build(HttpStatusEnum.OK, null);
    }

    public static <T> Result<T> ok(T data) {
        return build(HttpStatusEnum.OK, data);
    }

    public static <T> Result<T> ok(String message, T data) {
        Result<T> r = new Result<>();
        r.code = HttpStatusEnum.OK.getCode();
        r.message = message;
        r.data = data;
        r.timestamp = DateUtil.format(new Date(), DatePattern.NORM_DATETIME_PATTERN);
        return r;
    }

    // ==================== 失败 ====================

    public static <T> Result<T> fail() {
        return build(HttpStatusEnum.INTERNAL_ERROR, null);
    }

    public static <T> Result<T> fail(String message) {
        return build(HttpStatusEnum.INTERNAL_ERROR.getCode(), message, null);
    }

    public static <T> Result<T> fail(int code, String message) {
        return build(code, message, null);
    }

    public static <T> Result<T> fail(HttpStatusEnum status) {
        return build(status, null);
    }

    public static <T> Result<T> fail(HttpStatusEnum status, String message) {
        return build(status.getCode(), message, null);
    }

    // ==================== 构建 ====================

    private static <T> Result<T> build(HttpStatusEnum status, T data) {
        return build(status.getCode(), status.getMessage(), data);
    }

    private static <T> Result<T> build(int code, String message, T data) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = message;
        r.data = data;
        r.timestamp = DateUtil.format(new Date(), DatePattern.NORM_DATETIME_PATTERN);
        return r;
    }

    // ==================== getter/setter ====================

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
