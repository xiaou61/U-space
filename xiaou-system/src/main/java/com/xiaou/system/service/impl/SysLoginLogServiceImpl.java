package com.xiaou.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.xiaou.system.domain.SysLoginLog;
import com.xiaou.system.dto.LoginLogQueryRequest;
import com.xiaou.system.dto.LoginLogResponse;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.utils.PageHelper;
import com.xiaou.system.mapper.SysLoginLogMapper;
import com.xiaou.system.service.SysLoginLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 登录日志服务实现类
 *
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysLoginLogServiceImpl implements SysLoginLogService {

    private final SysLoginLogMapper loginLogMapper;

    @Override
    public PageResult<LoginLogResponse> getLoginLogPage(LoginLogQueryRequest query) {
        log.debug("分页查询登录日志，查询条件: {}", query);
        
        return PageHelper.doPage(
            query,
            loginLogMapper::selectPageCount,
            (request, offset, pageSize) -> {
                List<SysLoginLog> loginLogs = loginLogMapper.selectPageList(request, offset, pageSize);
                return loginLogs.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            }
        );
    }

    @Override
    public LoginLogResponse getById(Long id) {
        if (id == null) {
            return null;
        }
        
        SysLoginLog loginLog = loginLogMapper.selectById(id);
        if (loginLog == null) {
            return null;
        }
        
        return convertToResponse(loginLog);
    }

    @Override
    public boolean clearLoginLog() {
        try {
            loginLogMapper.truncate();
            log.info("登录日志已清空");
            return true;
        } catch (Exception e) {
            log.error("清空登录日志失败", e);
            return false;
        }
    }

    /**
     * 转换为响应DTO
     */
    private LoginLogResponse convertToResponse(SysLoginLog loginLog) {
        LoginLogResponse response = BeanUtil.copyProperties(loginLog, LoginLogResponse.class);
        return response;
    }
} 