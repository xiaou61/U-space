package com.xiaou.common.exception;

import cn.dev33.satoken.exception.DisableServiceException;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.xiaou.common.core.domain.Result;
import com.xiaou.common.core.domain.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;


import java.util.Set;

/**
 * 全局异常处理器
 *
 * @author xiaou
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常处理
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * Sa-Token 未登录异常处理
     */
    @ExceptionHandler(NotLoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleNotLoginException(NotLoginException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.warn("请求地址'{}',Token验证失败: {}", requestURI, e.getMessage());
        
        // 根据不同的异常类型返回不同的消息
        String message;
        switch (e.getType()) {
            case NotLoginException.NOT_TOKEN:
                message = "未提供Token，请先登录";
                break;
            case NotLoginException.INVALID_TOKEN:
                message = "Token无效，请重新登录";
                break;
            case NotLoginException.TOKEN_TIMEOUT:
                message = "Token已过期，请重新登录";
                break;
            case NotLoginException.BE_REPLACED:
                message = "Token已被顶替，请重新登录";
                break;
            case NotLoginException.KICK_OUT:
                message = "已被踢下线，请重新登录";
                break;
            default:
                message = "登录状态已失效，请重新登录";
        }
        
        return Result.error(ResultCode.TOKEN_INVALID.getCode(), message);
    }

    /**
     * Sa-Token 权限不足异常处理
     */
    @ExceptionHandler(NotPermissionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleNotPermissionException(NotPermissionException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.warn("请求地址'{}',权限不足: 缺少权限[{}]", requestURI, e.getPermission());
        return Result.error(ResultCode.FORBIDDEN.getCode(), "权限不足，无法访问");
    }

    /**
     * Sa-Token 角色权限不足异常处理
     */
    @ExceptionHandler(NotRoleException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleNotRoleException(NotRoleException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.warn("请求地址'{}',角色权限不足: 缺少角色[{}]", requestURI, e.getRole());
        return Result.error(ResultCode.FORBIDDEN.getCode(), "角色权限不足，无法访问");
    }

    /**
     * Sa-Token 服务封禁异常处理
     */
    @ExceptionHandler(DisableServiceException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleDisableServiceException(DisableServiceException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.warn("请求地址'{}',服务已被封禁: {}", requestURI, e.getMessage());
        return Result.error(ResultCode.ACCOUNT_DISABLED.getCode(), "账号已被封禁，请联系管理员");
    }

    /**
     * 请求方式不支持异常处理
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<Void> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e,
                                                            HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',不支持'{}'请求", requestURI, e.getMethod());
        return Result.error(ResultCode.METHOD_NOT_ALLOWED.getCode(), 
                          String.format("请求方法'%s'不支持", e.getMethod()));
    }

    /**
     * 请求媒体类型不支持异常处理
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public Result<Void> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException e,
                                                        HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',不支持'{}'媒体类型", requestURI, e.getContentType());
        return Result.error(ResultCode.UNSUPPORTED_MEDIA_TYPE.getCode(), "不支持的媒体类型");
    }

    /**
     * 路径变量缺失异常处理
     */
    @ExceptionHandler(MissingPathVariableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMissingPathVariable(MissingPathVariableException e,
                                                 HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',缺少必要的路径变量'{}'", requestURI, e.getVariableName());
        return Result.error(ResultCode.BAD_REQUEST.getCode(), 
                          String.format("缺少必要的路径变量[%s]", e.getVariableName()));
    }

    /**
     * 请求参数缺失异常处理
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMissingServletRequestParameter(MissingServletRequestParameterException e,
                                                            HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',缺少必要的请求参数'{}'", requestURI, e.getParameterName());
        return Result.error(ResultCode.BAD_REQUEST.getCode(), 
                          String.format("缺少必要的请求参数[%s]", e.getParameterName()));
    }

    /**
     * 参数类型不匹配异常处理
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e,
                                                         HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',请求参数类型不匹配,参数'{}'需要类型'{}'", 
                 requestURI, e.getName(), e.getRequiredType().getName());
        return Result.error(ResultCode.BAD_REQUEST.getCode(), 
                          String.format("参数类型不匹配,参数[%s]需要[%s]类型", e.getName(), e.getRequiredType().getSimpleName()));
    }

    /**
     * 请求参数验证异常处理
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
                                                     HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        log.error("请求地址'{}',参数校验失败'{}'", requestURI, message);
        return Result.error(ResultCode.PARAM_VALIDATE_ERROR.getCode(), message);
    }

    /**
     * 绑定异常处理
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e) {
        log.error("参数绑定校验异常", e);
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数绑定失败";
        return Result.error(ResultCode.PARAM_VALIDATE_ERROR.getCode(), message);
    }

    /**
     * 约束违反异常处理
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        log.error("参数校验失败", e);
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        String message = violations.isEmpty() ? "参数校验失败" : violations.iterator().next().getMessage();
        return Result.error(ResultCode.PARAM_VALIDATE_ERROR.getCode(), message);
    }

    /**
     * 404异常处理
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',无法找到对应的处理器", requestURI);
        return Result.error(ResultCode.NOT_FOUND.getCode(), String.format("请求地址'%s'不存在", requestURI));
    }

    /**
     * 系统异常处理
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',发生系统异常.", requestURI, e);
        return Result.error(ResultCode.ERROR.getCode(), "系统内部错误，请联系管理员");
    }
} 