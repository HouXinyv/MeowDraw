package com.miao.ai_gen_web.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException{
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

}
