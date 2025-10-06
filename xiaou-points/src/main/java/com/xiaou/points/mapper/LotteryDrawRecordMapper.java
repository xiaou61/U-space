package com.xiaou.points.mapper;

import com.xiaou.points.domain.LotteryDrawRecord;
import com.xiaou.points.dto.lottery.LotteryRecordQueryRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 抽奖记录Mapper
 * 
 * @author xiaou
 */
@Mapper
public interface LotteryDrawRecordMapper {
    
    /**
     * 插入抽奖记录
     */
    int insert(LotteryDrawRecord record);
    
    /**
     * 根据ID查询
     */
    LotteryDrawRecord selectById(@Param("id") Long id);
    
    /**
     * 查询用户抽奖记录（分页）
     */
    List<LotteryDrawRecord> selectByUserId(@Param("userId") Long userId, 
                                           @Param("request") LotteryRecordQueryRequest request);
    
    /**
     * 统计用户抽奖次数
     */
    Integer countByUserId(@Param("userId") Long userId);
    
    /**
     * 统计用户中奖次数
     */
    Integer countWinByUserId(@Param("userId") Long userId);
    
    /**
     * 查询所有记录（管理端分页）
     */
    List<LotteryDrawRecord> selectAll(@Param("request") LotteryRecordQueryRequest request);
    
    /**
     * 统计总记录数
     */
    Integer countAll();
    
    /**
     * 查询今日抽奖记录
     */
    List<LotteryDrawRecord> selectTodayRecords();
    
    /**
     * 查询用户最近N次抽奖时间
     */
    List<java.time.LocalDateTime> selectRecentDrawTimes(@Param("userId") Long userId, @Param("limit") Integer limit);
    
    /**
     * 查询用户最近N次抽奖设备
     */
    List<String> selectRecentDevices(@Param("userId") Long userId, @Param("limit") Integer limit);
}

