package com.xiaou.moyu.controller;

import com.xiaou.common.annotation.Log;
import com.xiaou.common.core.domain.Result;
import com.xiaou.common.exception.BusinessException;
import com.xiaou.common.utils.UserContextUtil;
import com.xiaou.moyu.dto.SalaryCalculatorDto;
import com.xiaou.moyu.dto.SalaryConfigRequest;
import com.xiaou.moyu.dto.WorkTimeRequest;
import com.xiaou.moyu.service.SalaryCalculatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 时薪计算器控制器
 *
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/moyu/salary-calculator")
@RequiredArgsConstructor
public class SalaryCalculatorController {
    
    private final SalaryCalculatorService salaryCalculatorService;
    private final UserContextUtil userContextUtil;
    
    /**
     * 获取时薪计算器数据
     */
    @Log(module = "摸鱼工具", type = Log.OperationType.SELECT, description = "获取时薪计算器数据")
    @GetMapping("/data")
    public Result<SalaryCalculatorDto> getSalaryCalculatorData() {
        Long userId = getCurrentUserId();
        SalaryCalculatorDto data = salaryCalculatorService.getSalaryCalculator(userId);
        return Result.success(data);
    }
    
    /**
     * 获取用户薪资配置
     */
    @Log(module = "摸鱼工具", type = Log.OperationType.SELECT, description = "获取用户薪资配置")
    @GetMapping("/config")
    public Result<SalaryConfigRequest> getSalaryConfig() {
        Long userId = getCurrentUserId();
        SalaryConfigRequest config = salaryCalculatorService.getSalaryConfig(userId);
        return Result.success(config);
    }
    
    /**
     * 保存或更新薪资配置
     */
    @Log(module = "摸鱼工具", type = Log.OperationType.UPDATE, description = "保存或更新薪资配置")
    @PostMapping("/config")
    public Result<Void> saveOrUpdateSalaryConfig(@Valid @RequestBody SalaryConfigRequest request) {
        Long userId = getCurrentUserId();
        boolean success = salaryCalculatorService.saveOrUpdateSalaryConfig(userId, request);
        if (!success) {
            throw new BusinessException("保存薪资配置失败");
        }
        return Result.success();
    }
    
    /**
     * 删除薪资配置
     */
    @Log(module = "摸鱼工具", type = Log.OperationType.DELETE, description = "删除薪资配置")
    @DeleteMapping("/config")
    public Result<Void> deleteSalaryConfig() {
        Long userId = getCurrentUserId();
        boolean success = salaryCalculatorService.deleteSalaryConfig(userId);
        if (!success) {
            throw new BusinessException("删除薪资配置失败");
        }
        return Result.success();
    }
    
    /**
     * 工作时间操作（开始/结束工作）
     */
    @Log(module = "摸鱼工具", type = Log.OperationType.UPDATE, description = "工作时间操作")
    @PostMapping("/work-time")
    public Result<SalaryCalculatorDto> handleWorkTime(@Valid @RequestBody WorkTimeRequest request) {
        Long userId = getCurrentUserId();
        SalaryCalculatorDto data = salaryCalculatorService.handleWorkTimeAction(userId, request);
        return Result.success(data);
    }
    
    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        Long userId = userContextUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        return userId;
    }
}
