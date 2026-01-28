package com.xiaou.sqloptimizer.service.impl;

import cn.hutool.json.JSONUtil;
import com.xiaou.ai.dto.sql.SqlAnalyzeResult;
import com.xiaou.ai.service.AiSqlOptimizeService;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.exception.BusinessException;
import com.xiaou.sqloptimizer.domain.SqlOptimizeRecord;
import com.xiaou.sqloptimizer.dto.SqlAnalyzeRequest;
import com.xiaou.sqloptimizer.mapper.SqlOptimizeRecordMapper;
import com.xiaou.sqloptimizer.service.SqlOptimizerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SQL优化服务实现
 *
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SqlOptimizerServiceImpl implements SqlOptimizerService {

    private final AiSqlOptimizeService aiSqlOptimizeService;
    private final SqlOptimizeRecordMapper recordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SqlAnalyzeResult analyze(Long userId, SqlAnalyzeRequest request) {
        log.info("用户{}开始分析SQL", userId);

        // 将表结构转为JSON字符串
        String tableStructuresJson = JSONUtil.toJsonStr(request.getTableStructures());

        // 调用AI服务分析
        SqlAnalyzeResult result = aiSqlOptimizeService.analyzeSql(
                request.getSql(),
                request.getExplainResult(),
                request.getExplainFormat(),
                tableStructuresJson,
                request.getMysqlVersion()
        );

        // 保存分析记录
        SqlOptimizeRecord record = new SqlOptimizeRecord()
                .setUserId(userId)
                .setOriginalSql(request.getSql())
                .setExplainResult(request.getExplainResult())
                .setExplainFormat(request.getExplainFormat())
                .setTableStructures(tableStructuresJson)
                .setMysqlVersion(request.getMysqlVersion())
                .setAnalysisResult(JSONUtil.toJsonStr(result))
                .setScore(result.getScore())
                .setIsFavorite(0)
                .setCreateTime(LocalDateTime.now())
                .setUpdateTime(LocalDateTime.now());

        recordMapper.insert(record);
        log.info("SQL分析完成，记录ID: {}, 评分: {}", record.getId(), result.getScore());

        return result;
    }

    @Override
    public PageResult<SqlOptimizeRecord> getHistory(Long userId, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<SqlOptimizeRecord> records = recordMapper.selectByUserId(userId, offset, pageSize);
        long total = recordMapper.countByUserId(userId);

        return PageResult.of(pageNum, pageSize, total, records);
    }

    @Override
    public SqlOptimizeRecord getById(Long userId, Long id) {
        SqlOptimizeRecord record = recordMapper.selectById(id);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException("记录不存在");
        }
        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleFavorite(Long userId, Long id) {
        SqlOptimizeRecord record = getById(userId, id);
        int newFavorite = record.getIsFavorite() == 1 ? 0 : 1;
        recordMapper.updateFavorite(id, userId, newFavorite);
        return newFavorite == 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long id) {
        getById(userId, id); // 验证权限
        recordMapper.deleteById(id, userId);
        log.info("用户{}删除SQL分析记录{}", userId, id);
    }
}
