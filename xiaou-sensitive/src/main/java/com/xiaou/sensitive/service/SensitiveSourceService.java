package com.xiaou.sensitive.service;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.sensitive.domain.SensitiveSource;
import com.xiaou.sensitive.dto.SensitiveSourceQuery;

/**
 * 敏感词来源管理服务接口
 *
 * @author xiaou
 */
public interface SensitiveSourceService {

    /**
     * 分页查询词库来源列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<SensitiveSource> listSources(SensitiveSourceQuery query);

    /**
     * 根据ID查询词库来源
     *
     * @param id 来源ID
     * @return 来源信息
     */
    SensitiveSource getSourceById(Integer id);

    /**
     * 新增词库来源
     *
     * @param source 来源信息
     * @return 是否成功
     */
    boolean addSource(SensitiveSource source);

    /**
     * 更新词库来源
     *
     * @param source 来源信息
     * @return 是否成功
     */
    boolean updateSource(SensitiveSource source);

    /**
     * 删除词库来源
     *
     * @param id 来源ID
     * @return 是否成功
     */
    boolean deleteSource(Integer id);

    /**
     * 测试连接
     *
     * @param id 来源ID
     * @return 测试结果消息
     */
    String testConnection(Integer id);

    /**
     * 手动同步词库
     *
     * @param id 来源ID
     * @return 同步结果
     */
    SyncResult syncSource(Integer id);

    /**
     * 同步结果
     */
    class SyncResult {
        private boolean success;
        private int addedCount;
        private int updatedCount;
        private int failedCount;
        private String message;

        public SyncResult(boolean success, int addedCount, int updatedCount, int failedCount, String message) {
            this.success = success;
            this.addedCount = addedCount;
            this.updatedCount = updatedCount;
            this.failedCount = failedCount;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public int getAddedCount() { return addedCount; }
        public int getUpdatedCount() { return updatedCount; }
        public int getFailedCount() { return failedCount; }
        public String getMessage() { return message; }
    }
}
