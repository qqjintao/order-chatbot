package com.chatbot.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

@Schema(description = "统一响应体")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "状态码")
    private Integer code;

    @Schema(description = "响应消息")
    private String message;

    @Schema(description = "响应数据")
    private T data;

    @Schema(description = "时间戳")
    private Long timestamp;

    private Result() {}

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
        r.timestamp = System.currentTimeMillis();
        return r;
    }

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

    private static <T> Result<T> build(HttpStatusEnum status, T data) {
        return build(status.getCode(), status.getMessage(), data);
    }

    private static <T> Result<T> build(int code, String message, T data) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = message;
        r.data = data;
        r.timestamp = System.currentTimeMillis();
        return r;
    }

    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
}