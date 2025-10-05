package com.xiaou.sensitive.mapper;

import com.xiaou.sensitive.domain.SensitiveHomophone;
import com.xiaou.sensitive.dto.SensitiveHomophoneQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 同音字映射Mapper
 *
 * @author xiaou
 */
@Mapper
public interface SensitiveHomophoneMapper {

    /**
     * 分页查询同音字列表
     *
     * @param query 查询条件
     * @return 同音字列表
     */
    List<SensitiveHomophone> selectHomophoneList(SensitiveHomophoneQuery query);

    /**
     * 根据ID查询同音字
     *
     * @param id 同音字ID
     * @return 同音字信息
     */
    SensitiveHomophone selectHomophoneById(Integer id);

    /**
     * 查询所有启用的同音字映射
     *
     * @return 同音字映射列表
     */
    List<SensitiveHomophone> selectEnabledHomophones();

    /**
     * 新增同音字
     *
     * @param homophone 同音字信息
     * @return 影响行数
     */
    int insertHomophone(SensitiveHomophone homophone);

    /**
     * 批量新增同音字
     *
     * @param list 同音字列表
     * @return 影响行数
     */
    int batchInsertHomophone(List<SensitiveHomophone> list);

    /**
     * 更新同音字
     *
     * @param homophone 同音字信息
     * @return 影响行数
     */
    int updateHomophone(SensitiveHomophone homophone);

    /**
     * 删除同音字
     *
     * @param id 同音字ID
     * @return 影响行数
     */
    int deleteHomophoneById(Integer id);
}
