package com.xiaou.system.service;

import com.xiaou.system.domain.SysOperationLog;
import com.xiaou.system.dto.OperationLogQueryRequest;
import com.xiaou.system.dto.OperationLogResponse;
import com.xiaou.system.dto.PageResult;

import java.util.List;

/**
 * 操作日志服务接口
 *
 * @author xiaou
 */
public interface SysOperationLogService {

    /**
     * 保存操作日志
     */
    void saveOperationLog(SysOperationLog operationLog);

    /**
     * 根据ID查询操作日志
     */
    OperationLogResponse getById(Long id);

    /**
     * 分页查询操作日志
     */
    PageResult<OperationLogResponse> getOperationLogPage(OperationLogQueryRequest query);

    /**
     * 批量删除操作日志
     */
    boolean deleteByIds(List<Long> ids);

    /**
     * 清空操作日志
     */
    boolean clearOperationLog();

    /**
     * 根据天数清理操作日志
     */
    boolean cleanOperationLogByDays(int days);
} 