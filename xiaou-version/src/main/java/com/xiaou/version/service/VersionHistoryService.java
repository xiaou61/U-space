package com.xiaou.version.service;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.version.dto.VersionHistoryCreateRequest;
import com.xiaou.version.dto.VersionHistoryQueryRequest;
import com.xiaou.version.dto.VersionHistoryResponse;
import com.xiaou.version.dto.VersionHistoryUpdateRequest;

import java.util.List;

/**
 * 版本历史服务接口
 * 
 * @author xiaou
 */
public interface VersionHistoryService {
    
    /**
     * 创建版本历史记录
     * 
     * @param request 创建请求
     * @return 版本历史记录ID
     */
    Long createVersion(VersionHistoryCreateRequest request);
    
    /**
     * 更新版本历史记录
     * 
     * @param request 更新请求
     * @return 是否成功
     */
    boolean updateVersion(VersionHistoryUpdateRequest request);
    
    /**
     * 根据ID删除版本历史记录（逻辑删除）
     * 
     * @param id 主键ID
     * @return 是否成功
     */
    boolean deleteVersion(Long id);
    
    /**
     * 根据ID查询版本历史记录详情
     * 
     * @param id 主键ID
     * @return 版本历史记录详情
     */
    VersionHistoryResponse getVersionDetail(Long id);
    
    /**
     * 管理端分页查询版本历史记录
     * 
     * @param request 查询条件
     * @return 分页结果
     */
    PageResult<VersionHistoryResponse> getAdminVersionList(VersionHistoryQueryRequest request);
    
    /**
     * 用户端分页查询版本历史记录（时间轴）
     * 
     * @param request 查询条件
     * @return 分页结果
     */
    PageResult<VersionHistoryResponse> getPublishedVersionList(VersionHistoryQueryRequest request);
    
    /**
     * 增加版本查看次数
     * 
     * @param id 主键ID
     * @return 是否成功
     */
    boolean incrementViewCount(Long id);
    
    /**
     * 发布版本
     * 
     * @param id 主键ID
     * @return 是否成功
     */
    boolean publishVersion(Long id);
    
    /**
     * 隐藏版本
     * 
     * @param id 主键ID
     * @return 是否成功
     */
    boolean hideVersion(Long id);
    
    /**
     * 取消发布（设置为草稿）
     * 
     * @param id 主键ID
     * @return 是否成功
     */
    boolean unpublishVersion(Long id);
    
    /**
     * 批量发布版本
     * 
     * @param ids ID列表
     * @return 是否成功
     */
    boolean batchPublishVersions(List<Long> ids);
    
    /**
     * 批量隐藏版本
     * 
     * @param ids ID列表
     * @return 是否成功
     */
    boolean batchHideVersions(List<Long> ids);
    
    /**
     * 批量删除版本
     * 
     * @param ids ID列表
     * @return 是否成功
     */
    boolean batchDeleteVersions(List<Long> ids);
    
    /**
     * 检查版本号是否已存在
     * 
     * @param versionNumber 版本号
     * @param excludeId 排除的ID（用于更新时检查）
     * @return 是否存在
     */
    boolean checkVersionNumberExists(String versionNumber, Long excludeId);
    
    /**
     * 获取最新的几个版本（用于首页展示等）
     * 
     * @param limit 限制数量
     * @return 版本列表
     */
    List<VersionHistoryResponse> getLatestVersions(int limit);
} 