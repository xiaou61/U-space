package com.xiaou.moyu.service;

import com.xiaou.moyu.dto.SalaryCalculatorDto;
import com.xiaou.moyu.dto.SalaryConfigRequest;
import com.xiaou.moyu.dto.WorkTimeRequest;

/**
 * 时薪计算器服务接口
 *
 * @author xiaou
 */
public interface SalaryCalculatorService {

    /**
     * 获取用户时薪计算器数据
     *
     * @param userId 用户ID
     * @return 时薪计算器数据
     */
    SalaryCalculatorDto getSalaryCalculator(Long userId);

    /**
     * 保存或更新用户薪资配置
     *
     * @param userId 用户ID
     * @param request 薪资配置请求
     * @return 是否成功
     */
    boolean saveOrUpdateSalaryConfig(Long userId, SalaryConfigRequest request);

    /**
     * 处理工作时间操作（开始/结束/暂停/恢复）
     *
     * @param userId 用户ID
     * @param request 工作时间操作请求
     * @return 操作后的计算器数据
     */
    SalaryCalculatorDto handleWorkTimeAction(Long userId, WorkTimeRequest request);

    /**
     * 获取用户薪资配置
     *
     * @param userId 用户ID
     * @return 薪资配置，如果不存在返回null
     */
    SalaryConfigRequest getSalaryConfig(Long userId);

    /**
     * 删除用户薪资配置
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteSalaryConfig(Long userId);
}
