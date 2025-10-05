package com.xiaou.sensitive.service;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.sensitive.domain.SensitiveHomophone;
import com.xiaou.sensitive.dto.SensitiveHomophoneQuery;

import java.util.List;

/**
 * 同音字映射服务接口
 *
 * @author xiaou
 */
public interface SensitiveHomophoneService {

    /**
     * 分页查询同音字列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<SensitiveHomophone> listHomophones(SensitiveHomophoneQuery query);

    /**
     * 根据ID查询同音字
     *
     * @param id 同音字ID
     * @return 同音字信息
     */
    SensitiveHomophone getHomophoneById(Integer id);

    /**
     * 新增同音字
     *
     * @param homophone 同音字信息
     * @return 是否成功
     */
    boolean addHomophone(SensitiveHomophone homophone);

    /**
     * 批量新增同音字
     *
     * @param homophones 同音字列表
     * @return 是否成功
     */
    boolean batchAddHomophones(List<SensitiveHomophone> homophones);

    /**
     * 更新同音字
     *
     * @param homophone 同音字信息
     * @return 是否成功
     */
    boolean updateHomophone(SensitiveHomophone homophone);

    /**
     * 删除同音字
     *
     * @param id 同音字ID
     * @return 是否成功
     */
    boolean deleteHomophone(Integer id);

    /**
     * 刷新同音字缓存
     */
    void refreshCache();
}
