package com.xiaou.points.strategy.impl;

import com.xiaou.points.domain.LotteryPrizeConfig;
import com.xiaou.points.strategy.LotteryStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 别名算法抽奖策略（Alias Method）
 * 时间复杂度O(1)，高性能抽奖算法
 * 
 * 算法原理：
 * 1. 预处理阶段构建概率表和别名表（O(n)复杂度）
 * 2. 抽奖阶段直接查表（O(1)复杂度）
 * 
 * @author xiaou
 */
@Slf4j
@Component
public class AliasMethodStrategy implements LotteryStrategy {
    
    private final Random random = new Random();
    
    @Override
    public LotteryPrizeConfig draw(Long userId, List<LotteryPrizeConfig> prizes) {
        if (prizes == null || prizes.isEmpty()) {
            throw new IllegalArgumentException("奖品列表不能为空");
        }
        
        // 构建别名表
        AliasTable aliasTable = buildAliasTable(prizes);
        
        // 使用别名方法抽奖
        LotteryPrizeConfig result = aliasTable.draw();
        
        log.info("用户{}使用Alias Method策略抽中奖品：{}", userId, result.getPrizeName());
        return result;
    }
    
    /**
     * 构建别名表
     */
    private AliasTable buildAliasTable(List<LotteryPrizeConfig> prizes) {
        int n = prizes.size();
        double[] prob = new double[n];
        int[] alias = new int[n];
        
        // 计算总概率
        BigDecimal totalProbability = prizes.stream()
                .map(LotteryPrizeConfig::getCurrentProbability)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 归一化概率并乘以 n（将概率转换为平均值为1的分布）
        double[] normalizedProb = new double[n];
        for (int i = 0; i < n; i++) {
            normalizedProb[i] = prizes.get(i).getCurrentProbability().doubleValue() 
                    * n / totalProbability.doubleValue();
        }
        
        // 分离小于1和大于等于1的概率
        List<Integer> small = new ArrayList<>();
        List<Integer> large = new ArrayList<>();
        
        for (int i = 0; i < n; i++) {
            if (normalizedProb[i] < 1.0) {
                small.add(i);
            } else {
                large.add(i);
            }
        }
        
        // 构建别名表
        while (!small.isEmpty() && !large.isEmpty()) {
            int less = small.remove(small.size() - 1);
            int more = large.remove(large.size() - 1);
            
            // 设置概率和别名
            prob[less] = normalizedProb[less];
            alias[less] = more;
            
            // 更新 more 的概率（减去填充给 less 的部分）
            normalizedProb[more] = normalizedProb[more] + normalizedProb[less] - 1.0;
            
            // 重新分类 more
            if (normalizedProb[more] < 1.0) {
                small.add(more);
            } else {
                large.add(more);
            }
        }
        
        // 处理剩余的元素（由于浮点数精度问题可能存在）
        while (!large.isEmpty()) {
            int index = large.remove(large.size() - 1);
            prob[index] = 1.0;
        }
        
        while (!small.isEmpty()) {
            int index = small.remove(small.size() - 1);
            prob[index] = 1.0;
        }
        
        return new AliasTable(prizes, prob, alias);
    }
    
    @Override
    public String getStrategyName() {
        return "ALIAS_METHOD";
    }
    
    /**
     * 别名表内部类
     */
    private class AliasTable {
        private final List<LotteryPrizeConfig> prizes;
        private final double[] prob;
        private final int[] alias;
        
        public AliasTable(List<LotteryPrizeConfig> prizes, double[] prob, int[] alias) {
            this.prizes = prizes;
            this.prob = prob;
            this.alias = alias;
        }
        
        /**
         * O(1) 时间复杂度抽奖
         */
        public LotteryPrizeConfig draw() {
            int n = prob.length;
            
            // 随机选择一个索引
            int column = random.nextInt(n);
            
            // 根据概率决定返回该列还是别名列
            boolean coinFlip = random.nextDouble() < prob[column];
            
            return prizes.get(coinFlip ? column : alias[column]);
        }
    }
}

