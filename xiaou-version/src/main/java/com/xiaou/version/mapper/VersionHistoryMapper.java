package com.xiaou.version.mapper;

import com.xiaou.version.domain.VersionHistory;
import com.xiaou.version.dto.VersionHistoryQueryRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 版本历史Mapper接口
 * 
 * @author xiaou
 */
@Mapper
public interface VersionHistoryMapper {
    
    /**
     * 插入版本历史记录
     * 
     * @param versionHistory 版本历史记录
     * @return 影响行数
     */
    int insert(VersionHistory versionHistory);
    
    /**
     * 根据ID删除（物理删除）
     * 
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 根据ID逻辑删除
     * 
     * @param id 主键ID
     * @param updatedBy 更新人ID
     * @return 影响行数
     */
    int logicalDeleteById(@Param("id") Long id, @Param("updatedBy") Long updatedBy);
    
    /**
     * 更新版本历史记录
     * 
     * @param versionHistory 版本历史记录
     * @return 影响行数
     */
    int updateById(VersionHistory versionHistory);
    
    /**
     * 根据ID查询版本历史记录
     * 
     * @param id 主键ID
     * @return 版本历史记录
     */
    VersionHistory selectById(@Param("id") Long id);
    
    /**
     * 根据版本号查询版本历史记录
     * 
     * @param versionNumber 版本号
     * @param deleted 是否删除
     * @return 版本历史记录
     */
    VersionHistory selectByVersionNumber(@Param("versionNumber") String versionNumber, @Param("deleted") Integer deleted);
    
    /**
     * 分页查询版本历史记录
     * 
     * @param request 查询条件
     * @return 版本历史记录列表
     */
    List<VersionHistory> selectByPage(VersionHistoryQueryRequest request);
    
    /**
     * 统计查询条件下的总数
     * 
     * @param request 查询条件
     * @return 总数
     */
    Long countByCondition(VersionHistoryQueryRequest request);
    
    /**
     * 查询已发布的版本历史记录（用户端时间轴）
     * 
     * @param request 查询条件
     * @return 版本历史记录列表
     */
    List<VersionHistory> selectPublishedByPage(VersionHistoryQueryRequest request);
    
    /**
     * 统计已发布版本总数（用户端时间轴）
     * 
     * @param request 查询条件
     * @return 总数
     */
    Long countPublishedByCondition(VersionHistoryQueryRequest request);
    
    /**
     * 增加查看次数
     * 
     * @param id 主键ID
     * @return 影响行数
     */
    int incrementViewCount(@Param("id") Long id);
    
    /**
     * 更新版本状态
     * 
     * @param id 主键ID
     * @param status 状态
     * @param updatedBy 更新人ID
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status, @Param("updatedBy") Long updatedBy);
    
    /**
     * 批量更新版本状态
     * 
     * @param ids ID列表
     * @param status 状态
     * @param updatedBy 更新人ID
     * @return 影响行数
     */
    int batchUpdateStatus(@Param("ids") List<Long> ids, @Param("status") Integer status, @Param("updatedBy") Long updatedBy);
    
    /**
     * 批量逻辑删除
     * 
     * @param ids ID列表
     * @param updatedBy 更新人ID
     * @return 影响行数
     */
    int batchLogicalDelete(@Param("ids") List<Long> ids, @Param("updatedBy") Long updatedBy);
} 