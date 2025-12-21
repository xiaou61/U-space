package com.xiaou.mockinterview.mapper;

import com.xiaou.mockinterview.domain.MockInterviewDirection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模拟面试方向配置Mapper
 *
 * @author xiaou
 */
@Mapper
public interface MockInterviewDirectionMapper {

    /**
     * 插入方向配置
     */
    int insert(MockInterviewDirection direction);

    /**
     * 根据ID更新方向配置
     */
    int updateById(MockInterviewDirection direction);

    /**
     * 根据ID查询方向配置
     */
    MockInterviewDirection selectById(Long id);

    /**
     * 根据方向代码查询配置
     */
    MockInterviewDirection selectByCode(String directionCode);

    /**
     * 查询所有启用的方向配置
     */
    List<MockInterviewDirection> selectAllEnabled();

    /**
     * 查询所有方向配置（包括禁用的）
     */
    List<MockInterviewDirection> selectAll();

    /**
     * 根据ID删除方向配置
     */
    int deleteById(Long id);

    /**
     * 更新方向状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
