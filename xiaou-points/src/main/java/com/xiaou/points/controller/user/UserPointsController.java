package com.xiaou.points.controller.user;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.core.domain.Result;
import com.xiaou.common.satoken.StpUserUtil;
import com.xiaou.points.dto.*;
import com.xiaou.points.service.PointsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端积分控制器
 * 
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/user/points")
@RequiredArgsConstructor
public class UserPointsController {
    
    private final PointsService pointsService;
    
    /**
     * 获取用户积分余额信息
     */
    @GetMapping("/balance")
    public Result<PointsBalanceResponse> getPointsBalance() {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("Token无效或已过期");
            }
            
            Long userId = StpUserUtil.getLoginIdAsLong();
            PointsBalanceResponse response = pointsService.getPointsBalance(userId);
            return Result.success("获取成功", response);
            
        } catch (Exception e) {
            log.error("获取用户积分余额失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 用户每日打卡
     */
    @PostMapping("/checkin")
    public Result<CheckinResponse> checkin() {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("Token无效或已过期");
            }
            
            Long userId = StpUserUtil.getLoginIdAsLong();
            CheckinResponse response = pointsService.checkin(userId);
            return Result.success("打卡成功", response);
            
        } catch (Exception e) {
            log.error("用户打卡失败，用户ID: {}", 
                    StpUserUtil.isLogin() ? StpUserUtil.getLoginIdAsLong() : null, e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取用户积分明细列表
     */
    @PostMapping("/detail")
    public Result<PageResult<PointsDetailResponse>> getPointsDetailList(@RequestBody PointsDetailQueryRequest request) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("Token无效或已过期");
            }
            
            Long userId = StpUserUtil.getLoginIdAsLong();
            // 设置当前用户ID，防止查询其他用户的数据
            request.setUserId(userId);
            
            PageResult<PointsDetailResponse> response = pointsService.getPointsDetailList(request);
            return Result.success("获取成功", response);
            
        } catch (Exception e) {
            log.error("获取积分明细失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取打卡日历
     */
    @PostMapping("/checkin-calendar")
    public Result<CheckinCalendarResponse> getCheckinCalendar(@RequestBody(required = false) CheckinCalendarRequest request) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            
            String yearMonth = request != null ? request.getYearMonth() : null;
            CheckinCalendarResponse response = pointsService.getCheckinCalendar(userId, yearMonth);
            return Result.success("获取成功", response);
            
        } catch (Exception e) {
            log.error("获取打卡日历失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取打卡统计数据
     */
    @PostMapping("/checkin-statistics")
    public Result<CheckinStatisticsResponse> getCheckinStatistics(@RequestBody(required = false) CheckinStatisticsRequest request) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            
            Integer months = request != null ? request.getMonths() : 3;
            if (months == null || months <= 0) {
                months = 3;
            }
            CheckinStatisticsResponse response = pointsService.getCheckinStatistics(userId, months);
            return Result.success("获取成功", response);
            
        } catch (Exception e) {
            log.error("获取打卡统计失败", e);
            return Result.error(e.getMessage());
        }
    }
}
