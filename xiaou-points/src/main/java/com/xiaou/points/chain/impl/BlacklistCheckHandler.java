package com.xiaou.points.chain.impl;

import com.xiaou.points.chain.RiskCheckHandler;
import com.xiaou.points.domain.UserLotteryLimit;
import com.xiaou.points.dto.lottery.LotteryContext;
import com.xiaou.points.mapper.UserLotteryLimitMapper;
import com.xiaou.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 黑名单检查处理器
 * 
 * @author xiaou
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BlacklistCheckHandler extends RiskCheckHandler {
    
    private final UserLotteryLimitMapper userLotteryLimitMapper;
    
    @Override
    public boolean check(Long userId, LotteryContext context) {
        UserLotteryLimit limit = userLotteryLimitMapper.selectByUserId(userId);
        
        if (limit != null && limit.getIsBlacklist() != null && limit.getIsBlacklist() == 1) {
            log.warn("用户{}在黑名单中，禁止抽奖", userId);
            throw new BusinessException("您的账号已被限制，无法参与抽奖");
        }
        
        log.debug("用户{}黑名单检查通过", userId);
        return checkNext(userId, context);
    }
}

