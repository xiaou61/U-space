package com.xiaou.filestorage.mapper;

import com.xiaou.filestorage.domain.FileSystemSetting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统设置数据访问接口
 *
 * @author xiaou
 */
@Mapper
public interface FileSystemSettingMapper {

    /**
     * 插入系统设置
     *
     * @param setting 系统设置
     * @return 影响行数
     */
    int insert(FileSystemSetting setting);

    /**
     * 根据ID查询设置
     *
     * @param id 设置ID
     * @return 系统设置
     */
    FileSystemSetting selectById(@Param("id") Long id);

    /**
     * 根据设置键查询
     *
     * @param settingKey 设置键
     * @return 系统设置
     */
    FileSystemSetting selectByKey(@Param("settingKey") String settingKey);

    /**
     * 查询所有设置
     *
     * @return 设置列表
     */
    List<FileSystemSetting> selectAll();

    /**
     * 更新系统设置
     *
     * @param setting 系统设置
     * @return 影响行数
     */
    int updateById(FileSystemSetting setting);

    /**
     * 删除系统设置
     *
     * @param id 设置ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据设置键删除
     *
     * @param settingKey 设置键
     * @return 影响行数
     */
    int deleteByKey(@Param("settingKey") String settingKey);
} 