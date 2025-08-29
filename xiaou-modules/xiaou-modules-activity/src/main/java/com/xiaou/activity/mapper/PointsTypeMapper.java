package com.xiaou.activity.mapper;

import com.xiaou.activity.domain.entity.PointsType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 积分类型Mapper接口
 */
@Mapper
public interface PointsTypeMapper {

    /**
     * 插入积分类型
     * @param pointsType 积分类型
     * @return 影响行数
     */
    int insert(PointsType pointsType);

    /**
     * 根据ID查询积分类型
     * @param id 积分类型ID
     * @return 积分类型
     */
    PointsType selectById(@Param("id") Long id);

    /**
     * 查询所有积分类型列表
     * @return 积分类型列表
     */
    List<PointsType> selectAll();

    /**
     * 查询启用的积分类型列表
     * @return 启用的积分类型列表
     */
    List<PointsType> selectActiveList();

    /**
     * 更新积分类型
     * @param pointsType 积分类型
     * @return 影响行数
     */
    int updateById(PointsType pointsType);

    /**
     * 更新积分类型状态
     * @param id 积分类型ID
     * @param status 状态
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 删除积分类型
     * @param id 积分类型ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);
} 