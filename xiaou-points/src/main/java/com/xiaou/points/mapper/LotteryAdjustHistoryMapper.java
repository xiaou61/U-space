package com.xiaou.points.mapper;

import com.xiaou.points.domain.LotteryAdjustHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 概率调整历史Mapper
 * 
 * @author xiaou
 */
@Mapper
public interface LotteryAdjustHistoryMapper {
    
    /**
     * 插入调整历史
     */
    int insert(LotteryAdjustHistory history);
    
    /**
     * 查询指定奖品的调整历史
     */
    List<LotteryAdjustHistory> selectByPrizeId(@Param("prizeId") Long prizeId);
    
    /**
     * 查询所有调整历史
     */
    List<LotteryAdjustHistory> selectAll();
}

