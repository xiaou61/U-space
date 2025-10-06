package com.xiaou.points.mapper;

import com.xiaou.points.domain.UserLotteryLimit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户抽奖限制Mapper
 * 
 * @author xiaou
 */
@Mapper
public interface UserLotteryLimitMapper {
    
    /**
     * 根据用户ID查询
     */
    UserLotteryLimit selectByUserId(@Param("userId") Long userId);
    
    /**
     * 插入记录
     */
    int insert(UserLotteryLimit limit);
    
    /**
     * 更新记录
     */
    int updateById(UserLotteryLimit limit);
    
    /**
     * 增加抽奖次数
     */
    int incrementDrawCount(@Param("userId") Long userId);
    
    /**
     * 增加中奖次数
     */
    int incrementWinCount(@Param("userId") Long userId);
    
    /**
     * 更新连续未中奖次数
     */
    int updateContinuousNoWin(@Param("userId") Long userId, 
                              @Param("count") Integer count);
    
    /**
     * 重置今日数据
     */
    int resetTodayData();
    
    /**
     * 重置所有用户的每日抽奖次数
     */
    int resetDailyCount();
    
    /**
     * 重置所有用户的周抽奖次数
     */
    int resetWeeklyCount();
    
    /**
     * 重置所有用户的月抽奖次数
     */
    int resetMonthlyCount();
    
    /**
     * 查询风险用户列表
     */
    List<UserLotteryLimit> selectRiskUsers(@Param("request") com.xiaou.points.dto.lottery.RiskUserQueryRequest request);
}

