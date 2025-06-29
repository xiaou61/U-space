package com.xiaou.common.exception;


import com.xiaou.common.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
}
