package com.xiaou.sensitive.mapper;

import com.xiaou.sensitive.domain.SensitiveHitStatistics;
import com.xiaou.sensitive.dto.HotWordVO;
import com.xiaou.sensitive.dto.SensitiveStatisticsQuery;
import com.xiaou.sensitive.dto.TrendDataVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 敏感词命中统计Mapper
 *
 * @author xiaou
 */
@Mapper
public interface SensitiveHitStatisticsMapper {

    /**
     * 根据日期和词查询统计
     *
     * @param statDate 统计日期
     * @param word 敏感词
     * @param module 业务模块
     * @return 统计信息
     */
    SensitiveHitStatistics selectByDateAndWord(@Param("statDate") LocalDate statDate, 
                                              @Param("word") String word, 
                                              @Param("module") String module);

    /**
     * 新增统计记录
     *
     * @param statistics 统计信息
     * @return 影响行数
     */
    int insert(SensitiveHitStatistics statistics);

    /**
     * 增加命中次数
     *
     * @param id 统计ID
     * @return 影响行数
     */
    int incrementHitCount(Long id);

    /**
     * 查询命中趋势
     *
     * @param query 查询条件
     * @return 趋势数据列表
     */
    List<TrendDataVO> selectTrend(SensitiveStatisticsQuery query);

    /**
     * 查询热门敏感词
     *
     * @param query 查询条件
     * @return 热门敏感词列表
     */
    List<HotWordVO> selectHotWords(SensitiveStatisticsQuery query);

    /**
     * 查询分类分布
     *
     * @param query 查询条件
     * @return 分类分布数据
     */
    List<Map<String, Object>> selectCategoryDistribution(SensitiveStatisticsQuery query);

    /**
     * 查询模块分布
     *
     * @param query 查询条件
     * @return 模块分布数据
     */
    List<Map<String, Object>> selectModuleDistribution(SensitiveStatisticsQuery query);

    /**
     * 查询总命中数
     *
     * @param query 查询条件
     * @return 总命中数
     */
    Long selectTotalHitCount(SensitiveStatisticsQuery query);
}
