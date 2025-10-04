package com.xiaou.points.service.impl;

import com.xiaou.common.utils.RedisUtil;
import com.xiaou.points.domain.LotteryPrizeConfig;
import com.xiaou.points.mapper.LotteryPrizeConfigMapper;
import com.xiaou.points.service.LotteryStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 抽奖库存管理服务实现
 * 
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LotteryStockServiceImpl implements LotteryStockService {
    
    private final LotteryPrizeConfigMapper prizeConfigMapper;
    private final RedisUtil redisUtil;
    
    /**
     * 库存缓存Key
     */
    private static final String STOCK_KEY = "lottery:prize:stock:";
    
    /**
     * 库存锁Key
     */
    private static final String STOCK_LOCK_KEY = "lottery:prize:stock:lock:";
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductStock(Long prizeId) {
        // 获取奖品配置
        LotteryPrizeConfig prize = prizeConfigMapper.selectById(prizeId);
        if (prize == null) {
            log.error("奖品{}不存在", prizeId);
            return false;
        }
        
        // 无库存限制的奖品，直接返回成功
        if (prize.getTotalStock() == null || prize.getTotalStock() < 0) {
            return true;
        }
        
        // 获取分布式锁
        String lockKey = STOCK_LOCK_KEY + prizeId;
        RLock lock = redisUtil.getLock(lockKey);
        
        try {
            // 尝试获取锁，最多等待3秒
            if (!lock.tryLock(3, 5, TimeUnit.SECONDS)) {
                log.warn("获取库存锁失败，奖品：{}", prizeId);
                return false;
            }
            
            // 1. 先从Redis扣减库存
            String stockKey = STOCK_KEY + prizeId;
            Object stockObj = redisUtil.get(stockKey);
            
            Integer currentStock;
            if (stockObj == null) {
                // Redis中没有库存信息，从数据库加载
                currentStock = prize.getCurrentStock() != null ? prize.getCurrentStock() : prize.getTotalStock();
                redisUtil.set(stockKey, currentStock);
            } else {
                currentStock = (Integer) stockObj;
            }
            
            // 检查库存
            if (currentStock <= 0) {
                log.warn("奖品{}库存不足", prizeId);
                return false;
            }
            
            // 扣减Redis库存
            long newStock = redisUtil.decr(stockKey, 1);
            if (newStock < 0) {
                // 扣减失败，回滚
                redisUtil.incr(stockKey, 1);
                log.warn("Redis库存扣减失败，奖品：{}", prizeId);
                return false;
            }
            
            // 2. 扣减数据库库存（异步或定时同步，这里为了保证一致性同步扣减）
            int updated = prizeConfigMapper.deductStock(prizeId);
            if (updated == 0) {
                // 数据库扣减失败，回滚Redis
                redisUtil.incr(stockKey, 1);
                log.error("数据库库存扣减失败，奖品：{}，回滚Redis库存", prizeId);
                return false;
            }
            
            log.info("奖品{}库存扣减成功，剩余：{}", prizeId, newStock);
            return true;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("库存扣减被中断", e);
            return false;
        } catch (Exception e) {
            log.error("库存扣减失败", e);
            return false;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rollbackStock(Long prizeId) {
        String stockKey = STOCK_KEY + prizeId;
        
        // 回滚Redis库存
        redisUtil.incr(stockKey, 1);
        
        // 回滚数据库库存
        prizeConfigMapper.increaseStock(prizeId);
        
        log.info("奖品{}库存已回滚", prizeId);
    }
    
    @Override
    public Integer getStock(Long prizeId) {
        String stockKey = STOCK_KEY + prizeId;
        Object stockObj = redisUtil.get(stockKey);
        
        if (stockObj != null) {
            return (Integer) stockObj;
        }
        
        // Redis中没有，从数据库读取
        LotteryPrizeConfig prize = prizeConfigMapper.selectById(prizeId);
        if (prize != null) {
            Integer stock = prize.getCurrentStock() != null ? prize.getCurrentStock() : prize.getTotalStock();
            if (stock != null) {
                redisUtil.set(stockKey, stock);
            }
            return stock;
        }
        
        return null;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncStockToDatabase() {
        log.info("开始同步Redis库存到数据库");
        
        List<LotteryPrizeConfig> prizes = prizeConfigMapper.selectAll();
        
        int syncCount = 0;
        for (LotteryPrizeConfig prize : prizes) {
            // 跳过无库存限制的奖品
            if (prize.getTotalStock() == null || prize.getTotalStock() < 0) {
                continue;
            }
            
            String stockKey = STOCK_KEY + prize.getId();
            Object stockObj = redisUtil.get(stockKey);
            
            if (stockObj != null) {
                Integer redisStock = (Integer) stockObj;
                Integer dbStock = prize.getCurrentStock();
                
                // 如果Redis和数据库库存不一致，以Redis为准
                if (dbStock == null || !redisStock.equals(dbStock)) {
                    prizeConfigMapper.updateStock(prize.getId(), redisStock);
                    syncCount++;
                    log.debug("同步奖品{}库存：{} -> {}", prize.getId(), dbStock, redisStock);
                }
            }
        }
        
        log.info("库存同步完成，同步{}个奖品", syncCount);
    }
}

