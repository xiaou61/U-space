package com.xiaou.sensitive.mapper;

import com.xiaou.sensitive.domain.SensitiveVersion;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 敏感词版本历史Mapper
 *
 * @author xiaou
 */
@Mapper
public interface SensitiveVersionMapper {

    /**
     * 查询版本历史列表
     *
     * @return 版本历史列表
     */
    List<SensitiveVersion> selectVersionList();

    /**
     * 根据ID查询版本
     *
     * @param id 版本ID
     * @return 版本信息
     */
    SensitiveVersion selectVersionById(Long id);

    /**
     * 查询最新版本
     *
     * @return 版本信息
     */
    SensitiveVersion selectLatestVersion();

    /**
     * 新增版本记录
     *
     * @param version 版本信息
     * @return 影响行数
     */
    int insertVersion(SensitiveVersion version);
}
