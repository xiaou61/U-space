package com.xiaou.system.mapper;

import com.xiaou.system.domain.SysOperationLog;
import com.xiaou.system.dto.OperationLogQueryRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 操作日志Mapper接口
 *
 * @author xiaou
 */
@Mapper
public interface SysOperationLogMapper {

    /**
     * 插入操作日志
     */
    int insert(SysOperationLog operationLog);

    /**
     * 根据ID查询操作日志
     */
    SysOperationLog selectById(Long id);

    /**
     * 查询操作日志列表
     */
    List<SysOperationLog> selectList(OperationLogQueryRequest query);

    /**
     * 查询操作日志总数
     */
    long selectCount(OperationLogQueryRequest query);

    /**
     * 批量删除操作日志
     */
    int deleteByIds(@Param("ids") List<Long> ids);

    /**
     * 清空操作日志
     */
    int deleteAll();

    /**
     * 根据时间范围删除操作日志
     */
    int deleteByTimeRange(@Param("days") int days);
} 