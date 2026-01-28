package com.xiaou.points.mapper;

import com.xiaou.points.domain.LotteryPrizeConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 抽奖奖品配置Mapper
 * 
 * @author xiaou
 */
@Mapper
public interface LotteryPrizeConfigMapper {
    
    /**
     * 查询所有启用的奖品配置
     */
    List<LotteryPrizeConfig> selectAllActive();
    
    /**
     * 根据ID查询
     */
    LotteryPrizeConfig selectById(@Param("id") Long id);
    
    /**
     * 查询所有奖品（包括禁用的）
     */
    List<LotteryPrizeConfig> selectAll();
    
    /**
     * 插入奖品配置
     */
    int insert(LotteryPrizeConfig config);
    
    /**
     * 更新奖品配置
     */
    int updateById(LotteryPrizeConfig config);
    
    /**
     * 删除奖品配置
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 更新当前概率
     */
    int updateCurrentProbability(@Param("id") Long id, 
                                  @Param("probability") BigDecimal probability);
    
    /**
     * 更新实际回报率
     */
    int updateActualReturnRate(@Param("id") Long id, 
                                @Param("returnRate") BigDecimal returnRate);
    
    /**
     * 更新中奖次数
     */
    int updateWinCount(@Param("id") Long id);
    
    /**
     * 更新抽奖次数
     */
    int updateDrawCount(@Param("id") Long id);
    
    /**
     * 增加抽取次数
     */
    int incrementDrawCount(@Param("prizeId") Long prizeId);
    
    /**
     * 增加中奖次数
     */
    int incrementWinCount(@Param("prizeId") Long prizeId);
    
    /**
     * 暂停奖品
     */
    int suspendPrize(@Param("id") Long id, 
                     @Param("reason") String reason, 
                     @Param("suspendUntil") String suspendUntil);
    
    /**
     * 恢复奖品
     */
    int resumePrize(@Param("id") Long id);
    
    /**
     * 重置今日数据
     */
    int resetTodayData();
    
    /**
     * 查询所有暂停的奖品
     */
    List<LotteryPrizeConfig> selectSuspended();
    
    /**
     * 重置每日统计
     */
    int resetDailyStats();
    
    /**
     * 扣减库存
     */
    int deductStock(@Param("id") Long id);
    
    /**
     * 增加库存（回滚用）
     */
    int increaseStock(@Param("id") Long id);
    
    /**
     * 更新库存
     */
    int updateStock(@Param("id") Long id, @Param("stock") Integer stock);
    
    /**
     * 批量查询奖品配置
     * @param ids ID列表
     * @return 奖品配置列表
     */
    List<LotteryPrizeConfig> selectBatchIds(@Param("ids") List<Long> ids);
    
    /**
     * 批量更新奖品启用状态
     * @param ids ID列表
     * @param isActive 是否启用
     * @return 影响行数
     */
    int batchUpdateIsActive(@Param("ids") List<Long> ids, @Param("isActive") Integer isActive);
}

