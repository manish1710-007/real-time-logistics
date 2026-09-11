package com.logistics.shared.exception;

public abstract class BusinessException extends RuntimeException {

    private final String code;
    private final String detail;

    protected BusinessException(String code, String message) {
        super(message);
        this.code = code;
        this.detail = null;
    }

    protected BusinessException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.detail = null;
    }

    public String getCode() {
        return code;
    }

    public String getDetail() {
        return detail;
    }
}
