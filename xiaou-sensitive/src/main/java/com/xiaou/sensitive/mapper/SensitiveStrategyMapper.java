package com.xiaou.sensitive.mapper;

import com.xiaou.sensitive.domain.SensitiveStrategy;
import com.xiaou.sensitive.dto.SensitiveStrategyQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 敏感词策略Mapper
 *
 * @author xiaou
 */
@Mapper
public interface SensitiveStrategyMapper {

    /**
     * 分页查询策略列表
     *
     * @param query 查询条件
     * @return 策略列表
     */
    List<SensitiveStrategy> selectStrategyList(SensitiveStrategyQuery query);

    /**
     * 根据ID查询策略
     *
     * @param id 策略ID
     * @return 策略信息
     */
    SensitiveStrategy selectStrategyById(Integer id);

    /**
     * 根据模块和等级查询策略
     *
     * @param module 模块名称
     * @param level 风险等级
     * @return 策略信息
     */
    SensitiveStrategy selectByModuleAndLevel(@Param("module") String module, 
                                            @Param("level") Integer level);

    /**
     * 查询所有启用的策略
     *
     * @return 策略列表
     */
    List<SensitiveStrategy> selectEnabledStrategies();

    /**
     * 新增策略
     *
     * @param strategy 策略信息
     * @return 影响行数
     */
    int insertStrategy(SensitiveStrategy strategy);

    /**
     * 更新策略
     *
     * @param strategy 策略信息
     * @return 影响行数
     */
    int updateStrategy(SensitiveStrategy strategy);

    /**
     * 删除策略
     *
     * @param id 策略ID
     * @return 影响行数
     */
    int deleteStrategyById(Integer id);
}
