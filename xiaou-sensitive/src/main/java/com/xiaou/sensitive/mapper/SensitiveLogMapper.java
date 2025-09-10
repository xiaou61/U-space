package com.xiaou.sensitive.mapper;

import com.xiaou.sensitive.domain.SensitiveLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 敏感词检测日志数据访问接口
 */
@Mapper
public interface SensitiveLogMapper {

    /**
     * 插入检测日志
     * @param log 日志信息
     * @return 影响行数
     */
    int insertLog(SensitiveLog log);
} 