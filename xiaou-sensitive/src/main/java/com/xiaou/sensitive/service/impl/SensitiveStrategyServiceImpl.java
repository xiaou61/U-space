package com.xiaou.sensitive.service.impl;

import cn.hutool.core.util.StrUtil;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.utils.PageHelper;
import com.xiaou.sensitive.domain.SensitiveStrategy;
import com.xiaou.sensitive.dto.SensitiveStrategyQuery;
import com.xiaou.sensitive.mapper.SensitiveStrategyMapper;
import com.xiaou.sensitive.service.SensitiveStrategyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 敏感词策略服务实现
 *
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SensitiveStrategyServiceImpl implements SensitiveStrategyService {

    private final SensitiveStrategyMapper strategyMapper;

    /**
     * 策略缓存
     */
    private Map<String, SensitiveStrategy> strategyCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // 初始化时加载所有策略
        refreshCache();
    }

    @Override
    public PageResult<SensitiveStrategy> listStrategy(SensitiveStrategyQuery query) {
        return PageHelper.doPage(query.getPageNum(), query.getPageSize(), () -> {
            return strategyMapper.selectStrategyList(query);
        });
    }

    @Override
    public SensitiveStrategy getStrategyById(Integer id) {
        return strategyMapper.selectStrategyById(id);
    }

    @Override
    public SensitiveStrategy getStrategy(String module, Integer level) {
        if (StrUtil.isBlank(module) || level == null) {
            return getDefaultStrategy(level);
        }

        // 构建缓存Key
        String key = buildCacheKey(module, level);

        // 从缓存获取
        SensitiveStrategy strategy = strategyCache.get(key);
        if (strategy != null) {
            return strategy;
        }

        // 缓存未命中，从数据库查询
        strategy = strategyMapper.selectByModuleAndLevel(module, level);
        if (strategy == null) {
            // 没有配置，使用默认策略
            strategy = getDefaultStrategy(level);
        }

        // 放入缓存
        if (strategy != null) {
            strategyCache.put(key, strategy);
        }

        return strategy;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStrategy(SensitiveStrategy strategy) {
        try {
            int result = strategyMapper.updateStrategy(strategy);
            if (result > 0) {
                // 清除缓存
                String key = buildCacheKey(strategy.getModule(), strategy.getLevel());
                strategyCache.remove(key);
                
                log.info("更新策略成功: module={}, level={}", strategy.getModule(), strategy.getLevel());
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("更新策略失败", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetStrategy(Integer id) {
        try {
            SensitiveStrategy strategy = strategyMapper.selectStrategyById(id);
            if (strategy == null) {
                return false;
            }

            // 获取默认策略
            SensitiveStrategy defaultStrategy = getDefaultStrategy(strategy.getLevel());
            if (defaultStrategy == null) {
                return false;
            }

            // 重置为默认值
            strategy.setAction(defaultStrategy.getAction());
            strategy.setNotifyAdmin(defaultStrategy.getNotifyAdmin());
            strategy.setLimitUser(defaultStrategy.getLimitUser());
            strategy.setLimitDuration(defaultStrategy.getLimitDuration());

            int result = strategyMapper.updateStrategy(strategy);
            if (result > 0) {
                // 清除缓存
                String key = buildCacheKey(strategy.getModule(), strategy.getLevel());
                strategyCache.remove(key);
                
                log.info("重置策略成功: id={}", id);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("重置策略失败", e);
            return false;
        }
    }

    @Override
    public void refreshCache() {
        try {
            // 清空缓存
            strategyCache.clear();

            // 加载所有启用的策略
            List<SensitiveStrategy> strategies = strategyMapper.selectEnabledStrategies();
            for (SensitiveStrategy strategy : strategies) {
                String key = buildCacheKey(strategy.getModule(), strategy.getLevel());
                strategyCache.put(key, strategy);
            }

            log.info("策略缓存刷新完成，加载策略数量: {}", strategies.size());
        } catch (Exception e) {
            log.error("刷新策略缓存失败", e);
        }
    }

    /**
     * 获取默认策略
     */
    private SensitiveStrategy getDefaultStrategy(Integer level) {
        SensitiveStrategy strategy = new SensitiveStrategy();
        strategy.setLevel(level);
        
        if (level == null) {
            level = 1;
        }

        switch (level) {
            case 1: // 低风险
                strategy.setAction("replace");
                strategy.setNotifyAdmin(0);
                strategy.setLimitUser(0);
                break;
            case 2: // 中风险
                strategy.setAction("replace");
                strategy.setNotifyAdmin(1);
                strategy.setLimitUser(0);
                break;
            case 3: // 高风险
                strategy.setAction("reject");
                strategy.setNotifyAdmin(1);
                strategy.setLimitUser(1);
                strategy.setLimitDuration(60);
                break;
            default:
                strategy.setAction("replace");
                strategy.setNotifyAdmin(0);
                strategy.setLimitUser(0);
        }

        return strategy;
    }

    /**
     * 构建缓存Key
     */
    private String buildCacheKey(String module, Integer level) {
        return module + ":" + level;
    }
}
