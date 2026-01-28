package com.xiaou.sqloptimizer.service;

import com.xiaou.ai.dto.sql.SqlAnalyzeResult;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.sqloptimizer.domain.SqlOptimizeRecord;
import com.xiaou.sqloptimizer.dto.SqlAnalyzeRequest;

/**
 * SQL优化服务接口
 *
 * @author xiaou
 */
public interface SqlOptimizerService {

    /**
     * 分析SQL
     *
     * @param userId  用户ID
     * @param request 分析请求
     * @return 分析结果
     */
    SqlAnalyzeResult analyze(Long userId, SqlAnalyzeRequest request);

    /**
     * 获取分析历史
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<SqlOptimizeRecord> getHistory(Long userId, int pageNum, int pageSize);

    /**
     * 获取记录详情
     *
     * @param userId 用户ID
     * @param id     记录ID
     * @return 记录详情
     */
    SqlOptimizeRecord getById(Long userId, Long id);

    /**
     * 切换收藏状态
     *
     * @param userId 用户ID
     * @param id     记录ID
     * @return 新的收藏状态
     */
    boolean toggleFavorite(Long userId, Long id);

    /**
     * 删除记录
     *
     * @param userId 用户ID
     * @param id     记录ID
     */
    void delete(Long userId, Long id);
}
