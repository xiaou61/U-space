package com.xiaou.common.exception;


import cn.dev33.satoken.exception.NotLoginException;
import com.xiaou.common.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.xiaou.common.exception.ErrorCode.NOT_LOGIN_ERROR;

/**
 * 全局异常捕捉类 用于全局拦截业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceException.class)
    public R<?> ServiceExceptionHandler(ServiceException e) {
        log.error(e.getMessage());
        return R.fail(e.getMessage());
    }

    @ExceptionHandler(NotLoginException.class)
    public R<?> NotLoginExceptionHandler(NotLoginException e) {
        log.error(e.getMessage());
        return R.fail(NOT_LOGIN_ERROR.getCode(), "未登录");
    }
}
