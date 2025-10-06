package com.xiaou.points.factory;

import com.xiaou.points.strategy.LotteryStrategy;
import com.xiaou.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 抽奖策略工厂
 * 
 * @author xiaou
 */
@Slf4j
@Component
public class LotteryStrategyFactory {
    
    private final Map<String, LotteryStrategy> strategyMap = new ConcurrentHashMap<>();
    
    @Autowired
    public LotteryStrategyFactory(List<LotteryStrategy> strategies) {
        strategies.forEach(strategy -> {
            strategyMap.put(strategy.getStrategyName(), strategy);
            log.info("注册抽奖策略：{}", strategy.getStrategyName());
        });
    }
    
    /**
     * 获取抽奖策略
     * 
     * @param strategyName 策略名称
     * @return 抽奖策略
     */
    public LotteryStrategy getStrategy(String strategyName) {
        LotteryStrategy strategy = strategyMap.get(strategyName);
        if (strategy == null) {
            throw new BusinessException("不支持的抽奖策略: " + strategyName);
        }
        return strategy;
    }
    
    /**
     * 动态选择策略
     * 默认策略逻辑可根据需要扩展
     * 
     * @param userId 用户ID
     * @return 抽奖策略
     */
    public LotteryStrategy selectStrategy(Long userId) {
        // 默认使用Alias Method策略
        // 可以根据用户级别、VIP状态等选择不同策略
        return getStrategy("ALIAS_METHOD");
    }
}

