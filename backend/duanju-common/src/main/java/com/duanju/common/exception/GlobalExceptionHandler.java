package com.duanju.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.duanju.common.constant.ResultCode;
import com.duanju.common.core.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Autowired(required = false)
    private MessageSource messageSource;

    private String msg(String key) {
        if (messageSource == null) return key;
        try {
            return messageSource.getMessage(key, null, key, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            return key;
        }
    }

    @ExceptionHandler(NotLoginException.class)
    public R<?> handleNotLoginException(NotLoginException e) {
        return R.fail(401, msg("error.login.required"));
    }

    @ExceptionHandler(NotPermissionException.class)
    public R<?> handleNotPermissionException(NotPermissionException e) {
        return R.fail(403, msg("error.permission.denied"));
    }

    @ExceptionHandler(ServiceException.class)
    public R<?> handleServiceException(ServiceException e) {
        log.warn("业务异常: code={}, msg={}", e.getCode(), e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<?> handleValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String rawMsg = fieldError != null ? fieldError.getDefaultMessage() : "error.param.invalid";
        String translated = msg(rawMsg);
        return R.fail(ResultCode.PARAM_ERROR, translated);
    }

    @ExceptionHandler(BindException.class)
    public R<?> handleBindException(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String rawMsg = fieldError != null ? fieldError.getDefaultMessage() : "error.param.invalid";
        String translated = msg(rawMsg);
        return R.fail(ResultCode.PARAM_ERROR, translated);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public R<?> handleIllegalArgumentException(IllegalArgumentException e) {
        return R.fail(ResultCode.PARAM_ERROR, e.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public R<?> handleNoResource(NoResourceFoundException e) {
        log.debug("接口不存在: {}", e.getResourcePath());
        return R.fail(404, msg("error.not.found") + ": " + e.getResourcePath());
    }

    @ExceptionHandler(Exception.class)
    public R<?> handleException(Exception e) {
        log.error("系统异常", e);
        return R.fail(msg("error.system.busy"));
    }
}
