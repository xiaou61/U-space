package com.xiaou.sensitive.mapper;

import com.xiaou.sensitive.domain.SensitiveSource;
import com.xiaou.sensitive.dto.SensitiveSourceQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 敏感词来源Mapper
 *
 * @author xiaou
 */
@Mapper
public interface SensitiveSourceMapper {

    /**
     * 分页查询来源列表
     *
     * @param query 查询条件
     * @return 来源列表
     */
    List<SensitiveSource> selectSourceList(SensitiveSourceQuery query);

    /**
     * 根据ID查询来源
     *
     * @param id 来源ID
     * @return 来源信息
     */
    SensitiveSource selectSourceById(Integer id);

    /**
     * 查询所有启用的来源
     *
     * @return 来源列表
     */
    List<SensitiveSource> selectEnabledSources();

    /**
     * 新增来源
     *
     * @param source 来源信息
     * @return 影响行数
     */
    int insertSource(SensitiveSource source);

    /**
     * 更新来源
     *
     * @param source 来源信息
     * @return 影响行数
     */
    int updateSource(SensitiveSource source);

    /**
     * 删除来源
     *
     * @param id 来源ID
     * @return 影响行数
     */
    int deleteSourceById(Integer id);
}
