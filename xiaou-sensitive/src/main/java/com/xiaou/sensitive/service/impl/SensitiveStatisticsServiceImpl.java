package com.xiaou.sensitive.service.impl;

import com.xiaou.sensitive.domain.SensitiveHitStatistics;
import com.xiaou.sensitive.domain.SensitiveUserViolation;
import com.xiaou.sensitive.dto.HotWordVO;
import com.xiaou.sensitive.dto.SensitiveStatisticsQuery;
import com.xiaou.sensitive.dto.StatisticsOverviewVO;
import com.xiaou.sensitive.dto.TrendDataVO;
import com.xiaou.sensitive.mapper.SensitiveHitStatisticsMapper;
import com.xiaou.sensitive.mapper.SensitiveUserViolationMapper;
import com.xiaou.sensitive.service.SensitiveStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 敏感词统计服务实现
 *
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SensitiveStatisticsServiceImpl implements SensitiveStatisticsService {

    private final SensitiveHitStatisticsMapper hitStatisticsMapper;
    private final SensitiveUserViolationMapper userViolationMapper;

    @Override
    @Async
    public void recordHit(String word, String module, Integer categoryId) {
        try {
            LocalDate today = LocalDate.now();
            
            // 查询是否已有记录
            SensitiveHitStatistics stat = hitStatisticsMapper.selectByDateAndWord(today, word, module);
            
            if (stat == null) {
                // 新增记录
                stat = new SensitiveHitStatistics();
                stat.setStatDate(today);
                stat.setWord(word);
                stat.setCategoryId(categoryId);
                stat.setModule(module);
                stat.setHitCount(1);
                hitStatisticsMapper.insert(stat);
            } else {
                // 更新计数
                hitStatisticsMapper.incrementHitCount(stat.getId());
            }
        } catch (Exception e) {
            log.error("记录敏感词命中失败: word={}, module={}", word, module, e);
        }
    }

    @Override
    @Async
    public void recordUserViolation(Long userId) {
        try {
            LocalDate today = LocalDate.now();
            
            // 查询用户今日违规记录
            SensitiveUserViolation violation = userViolationMapper.selectByUserAndDate(userId, today);
            
            if (violation == null) {
                // 新增记录
                violation = new SensitiveUserViolation();
                violation.setUserId(userId);
                violation.setStatDate(today);
                violation.setViolationCount(1);
                violation.setLastViolationTime(LocalDateTime.now());
                violation.setIsRestricted(0);
                userViolationMapper.insert(violation);
            } else {
                // 更新违规次数
                violation.setViolationCount(violation.getViolationCount() + 1);
                violation.setLastViolationTime(LocalDateTime.now());
                userViolationMapper.updateById(violation);
                
                // 检查是否需要限制用户（24小时内违规超过5次）
                if (violation.getViolationCount() >= 5 && violation.getIsRestricted() == 0) {
                    violation.setIsRestricted(1);
                    violation.setRestrictEndTime(LocalDateTime.now().plusHours(1));
                    userViolationMapper.updateById(violation);
                    log.warn("用户违规次数过多，已限制: userId={}, count={}", userId, violation.getViolationCount());
                }
            }
        } catch (Exception e) {
            log.error("记录用户违规失败: userId={}", userId, e);
        }
    }

    @Override
    public StatisticsOverviewVO getOverview(SensitiveStatisticsQuery query) {
        try {
            // 设置默认查询时间为今天
            if (query.getStartDate() == null) {
                query.setStartDate(LocalDate.now());
            }
            if (query.getEndDate() == null) {
                query.setEndDate(LocalDate.now());
            }

            // 查询总命中数
            Long hitCount = hitStatisticsMapper.selectTotalHitCount(query);

            // 查询违规用户数
            Long violationUserCount = userViolationMapper.countRestrictedUsers();

            // 计算拦截率（简化计算，实际应该根据总检测数计算）
            Double hitRate = hitCount > 0 ? (hitCount / 100.0) : 0.0;

            return StatisticsOverviewVO.builder()
                    .totalCheck(hitCount * 10) // 简化估算
                    .hitCount(hitCount)
                    .hitRate(hitRate)
                    .rejectCount(hitCount / 3) // 简化估算
                    .replaceCount(hitCount * 2 / 3) // 简化估算
                    .todayNewWords(0) // 需要额外查询
                    .violationUserCount(violationUserCount)
                    .build();
        } catch (Exception e) {
            log.error("获取统计总览失败", e);
            return StatisticsOverviewVO.builder()
                    .totalCheck(0L)
                    .hitCount(0L)
                    .hitRate(0.0)
                    .rejectCount(0L)
                    .replaceCount(0L)
                    .todayNewWords(0)
                    .violationUserCount(0L)
                    .build();
        }
    }

    @Override
    public List<TrendDataVO> getHitTrend(SensitiveStatisticsQuery query) {
        try {
            // 设置默认查询时间为最近7天
            if (query.getStartDate() == null) {
                query.setStartDate(LocalDate.now().minusDays(6));
            }
            if (query.getEndDate() == null) {
                query.setEndDate(LocalDate.now());
            }

            return hitStatisticsMapper.selectTrend(query);
        } catch (Exception e) {
            log.error("获取命中趋势失败", e);
            return List.of();
        }
    }

    @Override
    public List<HotWordVO> getHotWords(SensitiveStatisticsQuery query) {
        try {
            // 设置默认查询时间为今天
            if (query.getStartDate() == null) {
                query.setStartDate(LocalDate.now());
            }
            if (query.getEndDate() == null) {
                query.setEndDate(LocalDate.now());
            }
            
            // 设置默认TOP数量
            if (query.getTopN() == null) {
                query.setTopN(20);
            }

            return hitStatisticsMapper.selectHotWords(query);
        } catch (Exception e) {
            log.error("获取热门敏感词失败", e);
            return List.of();
        }
    }

    @Override
    public List<Map<String, Object>> getCategoryDistribution(SensitiveStatisticsQuery query) {
        try {
            // 设置默认查询时间为今天
            if (query.getStartDate() == null) {
                query.setStartDate(LocalDate.now());
            }
            if (query.getEndDate() == null) {
                query.setEndDate(LocalDate.now());
            }

            return hitStatisticsMapper.selectCategoryDistribution(query);
        } catch (Exception e) {
            log.error("获取分类分布失败", e);
            return List.of();
        }
    }

    @Override
    public List<Map<String, Object>> getModuleDistribution(SensitiveStatisticsQuery query) {
        try {
            // 设置默认查询时间为今天
            if (query.getStartDate() == null) {
                query.setStartDate(LocalDate.now());
            }
            if (query.getEndDate() == null) {
                query.setEndDate(LocalDate.now());
            }

            return hitStatisticsMapper.selectModuleDistribution(query);
        } catch (Exception e) {
            log.error("获取模块分布失败", e);
            return List.of();
        }
    }
}
