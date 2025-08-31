package com.xiaou.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.xiaou.system.domain.SysLoginLog;
import com.xiaou.system.dto.LoginLogQueryRequest;
import com.xiaou.system.dto.LoginLogResponse;
import com.xiaou.common.core.domain.PageResult;
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
        // 参数校验和默认值设置
        if (query.getPageNum() == null || query.getPageNum() < 1) {
            query.setPageNum(1);
        }
        if (query.getPageSize() == null || query.getPageSize() < 1) {
            query.setPageSize(10);
        }
        if (query.getPageSize() > 100) {
            query.setPageSize(100); // 限制最大页面大小
        }

        // 计算偏移量
        int offset = (query.getPageNum() - 1) * query.getPageSize();

        // 查询总数
        Long total = loginLogMapper.selectPageCount(query);

        // 查询数据
        List<SysLoginLog> loginLogs = loginLogMapper.selectPageList(query, offset, query.getPageSize());

        // 转换为响应DTO
        List<LoginLogResponse> responses = loginLogs.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return PageResult.of(query.getPageNum(), query.getPageSize(), total, responses);
    }

    @Override
    public LoginLogResponse getById(Long id) {
        if (id == null) {
            return null;
        }
        SysLoginLog loginLog = loginLogMapper.selectById(id);
        return loginLog != null ? convertToResponse(loginLog) : null;
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
        LoginLogResponse response = new LoginLogResponse();
        BeanUtil.copyProperties(loginLog, response);
        
        // 设置登录状态描述
        response.setLoginStatus(loginLog.getLoginStatus());
        
        return response;
    }
} 