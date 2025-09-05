package com.xiaou.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.xiaou.system.domain.SysOperationLog;
import com.xiaou.system.dto.OperationLogQueryRequest;
import com.xiaou.system.dto.OperationLogResponse;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.utils.PageHelper;
import com.xiaou.system.mapper.SysOperationLogMapper;
import com.xiaou.system.service.SysOperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 操作日志服务实现类
 *
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysOperationLogServiceImpl implements SysOperationLogService {

    private final SysOperationLogMapper operationLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOperationLog(SysOperationLog operationLog) {
        try {
            operationLogMapper.insert(operationLog);
            log.debug("保存操作日志成功: {}", operationLog.getOperationId());
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
            // 操作日志保存失败不应影响业务操作，所以这里只记录日志
        }
    }

    @Override
    public OperationLogResponse getById(Long id) {
        SysOperationLog operationLog = operationLogMapper.selectById(id);
        if (operationLog == null) {
            return null;
        }
        return convertToResponse(operationLog);
    }

    @Override
    public PageResult<OperationLogResponse> getOperationLogPage(OperationLogQueryRequest query) {
        return PageHelper.doPage(query.getPageNum(), query.getPageSize(), () -> {
            List<SysOperationLog> logList = operationLogMapper.selectList(query);
            return logList.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByIds(List<Long> ids) {
        try {
            int result = operationLogMapper.deleteByIds(ids);
            log.info("批量删除操作日志: {} 条", result);
            return result > 0;
        } catch (Exception e) {
            log.error("批量删除操作日志失败", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean clearOperationLog() {
        try {
            int result = operationLogMapper.deleteAll();
            log.info("清空操作日志: {} 条", result);
            return true;
        } catch (Exception e) {
            log.error("清空操作日志失败", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cleanOperationLogByDays(int days) {
        try {
            int result = operationLogMapper.deleteByTimeRange(days);
            log.info("清理{}天前的操作日志: {} 条", days, result);
            return result >= 0;
        } catch (Exception e) {
            log.error("清理操作日志失败", e);
            return false;
        }
    }

    /**
     * 转换为响应对象
     */
    private OperationLogResponse convertToResponse(SysOperationLog operationLog) {
        OperationLogResponse response = new OperationLogResponse();
        BeanUtil.copyProperties(operationLog, response);
        
        // 设置操作类型描述和状态描述
        response.setOperationType(operationLog.getOperationType());
        response.setStatus(operationLog.getStatus());
        
        return response;
    }
} 