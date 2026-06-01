package com.ai.examples.chatbot.common;

/**
 * 业务异常，Service 层抛出，由 GlobalExceptionHandler 统一捕获并转为 Result
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = HttpStatusEnum.INTERNAL_ERROR.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(HttpStatusEnum status) {
        super(status.getMessage());
        this.code = status.getCode();
    }

    public BusinessException(HttpStatusEnum status, String message) {
        super(message);
        this.code = status.getCode();
    }

    public int getCode() {
        return code;
    }
}
