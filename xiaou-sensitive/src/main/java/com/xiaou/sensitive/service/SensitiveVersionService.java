package com.xiaou.sensitive.service;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.sensitive.domain.SensitiveVersion;

/**
 * 敏感词版本管理服务接口
 *
 * @author xiaou
 */
public interface SensitiveVersionService {

    /**
     * 分页查询版本历史
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<SensitiveVersion> listVersions(Integer pageNum, Integer pageSize);

    /**
     * 创建新版本记录
     *
     * @param changeType 变更类型
     * @param changeCount 变更数量
     * @param changeDetail 变更详情
     * @param operatorId 操作人ID
     * @param remark 备注
     * @return 是否成功
     */
    boolean createVersion(String changeType, Integer changeCount, String changeDetail, 
                         Long operatorId, String remark);

    /**
     * 回滚到指定版本
     *
     * @param versionId 版本ID
     * @param operatorId 操作人ID
     * @return 是否成功
     */
    boolean rollbackToVersion(Long versionId, Long operatorId);

    /**
     * 获取版本详情
     *
     * @param versionId 版本ID
     * @return 版本信息
     */
    SensitiveVersion getVersionById(Long versionId);

    /**
     * 获取最新版本号
     *
     * @return 版本号
     */
    String getLatestVersionNo();
}
