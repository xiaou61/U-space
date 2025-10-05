package com.xiaou.sensitive.service;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.sensitive.domain.SensitiveStrategy;
import com.xiaou.sensitive.dto.SensitiveStrategyQuery;

/**
 * 敏感词策略服务接口
 *
 * @author xiaou
 */
public interface SensitiveStrategyService {

    /**
     * 分页查询策略列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<SensitiveStrategy> listStrategy(SensitiveStrategyQuery query);

    /**
     * 根据ID查询策略
     *
     * @param id 策略ID
     * @return 策略信息
     */
    SensitiveStrategy getStrategyById(Integer id);

    /**
     * 根据模块和等级获取策略
     *
     * @param module 模块名称
     * @param level 风险等级
     * @return 策略信息
     */
    SensitiveStrategy getStrategy(String module, Integer level);

    /**
     * 更新策略
     *
     * @param strategy 策略信息
     * @return 是否成功
     */
    boolean updateStrategy(SensitiveStrategy strategy);

    /**
     * 重置策略为默认值
     *
     * @param id 策略ID
     * @return 是否成功
     */
    boolean resetStrategy(Integer id);

    /**
     * 刷新策略缓存
     */
    void refreshCache();
}
