package com.xiaou.system.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.xiaou.system.log.domain.entity.SysOperLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysOperLogMapper extends BaseMapper<SysOperLog> {
}
