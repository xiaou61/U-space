package com.xiaou.system.service;

import com.xiaou.system.dto.LoginLogQueryRequest;
import com.xiaou.system.dto.LoginLogResponse;
import com.xiaou.common.core.domain.PageResult;

/**
 * 登录日志服务接口
 *
 * @author xiaou
 */
public interface SysLoginLogService {

    /**
     * 分页查询登录日志
     */
    PageResult<LoginLogResponse> getLoginLogPage(LoginLogQueryRequest query);

    /**
     * 根据ID查询登录日志
     */
    LoginLogResponse getById(Long id);

    /**
     * 清空登录日志
     */
    boolean clearLoginLog();
} 