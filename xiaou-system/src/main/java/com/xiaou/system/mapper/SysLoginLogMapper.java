package com.xiaou.system.mapper;

import com.xiaou.system.domain.SysLoginLog;
import com.xiaou.system.dto.LoginLogQueryRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 登录日志Mapper接口
 *
 * @author xiaou
 */
@Mapper
public interface SysLoginLogMapper {

    /**
     * 根据ID查询登录日志
     */
    SysLoginLog selectById(@Param("id") Long id);

    /**
     * 查询登录日志列表
     */
    List<SysLoginLog> selectList(@Param("loginLog") SysLoginLog loginLog);

    /**
     * 查询登录日志总数
     */
    Long selectCount(@Param("loginLog") SysLoginLog loginLog);

    /**
     * 新增登录日志
     */
    int insert(@Param("loginLog") SysLoginLog loginLog);

    /**
     * 根据ID删除登录日志
     */
    int deleteById(@Param("id") Long id);

    /**
     * 批量删除登录日志
     */
    int deleteByIds(@Param("ids") List<Long> ids);

    /**
     * 清空登录日志
     */
    int truncate();

    /**
     * 分页查询登录日志列表
     */
    List<SysLoginLog> selectPageList(@Param("query") LoginLogQueryRequest query, 
                                    @Param("offset") Integer offset, 
                                    @Param("size") Integer size);

    /**
     * 查询登录日志总数（带条件）
     */
    Long selectPageCount(@Param("query") LoginLogQueryRequest query);
} 