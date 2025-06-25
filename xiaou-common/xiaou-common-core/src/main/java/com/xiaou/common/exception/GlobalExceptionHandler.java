package com.xiaou.common.exception;


import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotRoleException;
import com.xiaou.common.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    @ExceptionHandler(BusinessException.class)
    public R<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return R.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(NotLoginException.class)
    public R<?> notLoginExceptionHandler(NotLoginException e) {
        log.error("NotLoginException", e);
        return R.fail("未登录");
    }

    @ExceptionHandler(ServiceException.class)
    public R<?> ServiceExceptionHandler(ServiceException e) {
        log.error(e.getMessage());
        return R.fail(e.getMessage());
    }

    @ExceptionHandler(NotRoleException.class)
    public R<?> notRoleExceptionHandler(NotRoleException e) {
        log.error("NotRoleException", e);
        return R.fail("无权限");
    }

}
