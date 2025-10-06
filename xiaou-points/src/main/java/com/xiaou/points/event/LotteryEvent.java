package com.xiaou.points.event;

import com.xiaou.points.domain.LotteryDrawRecord;
import lombok.Getter;

/**
 * 抽奖事件
 * 
 * @author xiaou
 */
@Getter
public class LotteryEvent {
    
    /**
     * 事件类型
     */
    private final EventType type;
    
    /**
     * 抽奖记录
     */
    private final LotteryDrawRecord drawRecord;
    
    /**
     * 事件数据
     */
    private final Object data;
    
    public LotteryEvent(EventType type, LotteryDrawRecord drawRecord) {
        this(type, drawRecord, null);
    }
    
    public LotteryEvent(EventType type, LotteryDrawRecord drawRecord, Object data) {
        this.type = type;
        this.drawRecord = drawRecord;
        this.data = data;
    }
    
    /**
     * 事件类型枚举
     */
    public enum EventType {
        /**
         * 抽奖完成
         */
        DRAW_COMPLETED,
        
        /**
         * 中奖
         */
        WIN,
        
        /**
         * 未中奖
         */
        NO_WIN,
        
        /**
         * 概率调整
         */
        PROBABILITY_ADJUSTED,
        
        /**
         * 奖品暂停
         */
        PRIZE_SUSPENDED
    }
}

