package com.xiaou.points.controller.user;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.core.domain.Result;
import com.xiaou.common.satoken.StpUserUtil;
import com.xiaou.common.utils.IPUtil;
import com.xiaou.points.dto.lottery.*;
import com.xiaou.points.service.LotteryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端抽奖控制器
 * 
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/user/lottery")
@RequiredArgsConstructor
public class UserLotteryController {
    
    private final LotteryService lotteryService;
    
    /**
     * 执行抽奖
     */
    @PostMapping("/draw")
    public Result<LotteryDrawResponse> draw(@RequestBody LotteryDrawRequest request, 
                                              HttpServletRequest httpRequest) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("Token无效或已过期");
            }
            
            Long userId = StpUserUtil.getLoginIdAsLong();
            String ip = IPUtil.getIpAddress(httpRequest);
            String device = httpRequest.getHeader("User-Agent");
            
            LotteryDrawResponse response = lotteryService.draw(request, userId, ip, device);
            
            log.info("用户{}抽奖成功，获得：{}", userId, response.getPrizeName());
            
            return Result.success("抽奖成功", response);
            
        } catch (Exception e) {
            log.error("用户抽奖失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取奖品列表
     */
    @GetMapping("/prizes")
    public Result<List<LotteryPrizeResponse>> getPrizeList() {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("Token无效或已过期");
            }
            
            List<LotteryPrizeResponse> prizes = lotteryService.getPrizeList();
            return Result.success("获取成功", prizes);
            
        } catch (Exception e) {
            log.error("获取奖品列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取用户抽奖记录
     */
    @PostMapping("/records")
    public Result<PageResult<LotteryDrawResponse>> getDrawRecords(@RequestBody LotteryRecordQueryRequest request) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("Token无效或已过期");
            }
            
            Long userId = StpUserUtil.getLoginIdAsLong();
            PageResult<LotteryDrawResponse> response = lotteryService.getUserDrawRecords(request, userId);
            return Result.success("获取成功", response);
            
        } catch (Exception e) {
            log.error("获取抽奖记录失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取用户抽奖统计
     */
    @GetMapping("/statistics")
    public Result<LotteryStatisticsResponse> getStatistics() {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("Token无效或已过期");
            }
            
            Long userId = StpUserUtil.getLoginIdAsLong();
            LotteryStatisticsResponse response = lotteryService.getUserStatistics(userId);
            return Result.success("获取成功", response);
            
        } catch (Exception e) {
            log.error("获取抽奖统计失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取抽奖规则
     */
    @GetMapping("/rules")
    public Result<String> getLotteryRules() {
        try {
            String rules = lotteryService.getLotteryRules();
            return Result.success("获取成功", rules);
        } catch (Exception e) {
            log.error("获取抽奖规则失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取今日剩余抽奖次数
     */
    @GetMapping("/remaining-count")
    public Result<Integer> getRemainingCount() {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("Token无效或已过期");
            }
            
            Long userId = StpUserUtil.getLoginIdAsLong();
            Integer count = lotteryService.getTodayRemainingCount(userId);
            return Result.success("获取成功", count);
            
        } catch (Exception e) {
            log.error("获取剩余抽奖次数失败", e);
            return Result.error(e.getMessage());
        }
    }
}

