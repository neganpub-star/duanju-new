package com.duanju.common.exception;

import com.duanju.common.constant.ResultCode;
import lombok.Getter;

@Getter
public class ServiceException extends RuntimeException {

    private final int code;

    public ServiceException(String message) {
        super(message);
        this.code = ResultCode.FAIL;
    }

    public ServiceException(int code, String message) {
        super(message);
        this.code = code;
    }

    public static ServiceException of(String message) {
        return new ServiceException(message);
    }

    public static ServiceException unauthorized(String message) {
        return new ServiceException(ResultCode.UNAUTHORIZED, message);
    }

    public static ServiceException forbidden(String message) {
        return new ServiceException(ResultCode.FORBIDDEN, message);
    }

    public static ServiceException notFound(String message) {
        return new ServiceException(ResultCode.NOT_FOUND, message);
    }
}
