package com.xiaou.points.chain;

import com.xiaou.points.dto.lottery.LotteryContext;

/**
 * 风控检查处理器（抽象类）
 * 
 * @author xiaou
 */
public abstract class RiskCheckHandler {
    
    protected RiskCheckHandler next;
    
    public void setNext(RiskCheckHandler next) {
        this.next = next;
    }
    
    /**
     * 检查逻辑
     * 
     * @param userId 用户ID
     * @param context 抽奖上下文
     * @return 是否通过检查
     */
    public abstract boolean check(Long userId, LotteryContext context);
    
    /**
     * 执行下一个处理器
     */
    protected boolean checkNext(Long userId, LotteryContext context) {
        if (next == null) {
            return true;
        }
        return next.check(userId, context);
    }
}

