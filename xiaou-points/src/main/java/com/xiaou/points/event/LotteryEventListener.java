package com.xiaou.points.event;

/**
 * 抽奖事件监听器接口
 * 
 * @author xiaou
 */
public interface LotteryEventListener {
    
    /**
     * 处理事件
     */
    void onEvent(LotteryEvent event);
    
    /**
     * 获取监听器名称
     */
    String getListenerName();
}

