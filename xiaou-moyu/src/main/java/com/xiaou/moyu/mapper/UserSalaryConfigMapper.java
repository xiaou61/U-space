package com.xiaou.moyu.mapper;

import com.xiaou.moyu.domain.UserSalaryConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户薪资配置数据访问层
 *
 * @author xiaou
 */
@Mapper
public interface UserSalaryConfigMapper {

    /**
     * 根据用户ID查询薪资配置
     *
     * @param userId 用户ID
     * @return 用户薪资配置
     */
    UserSalaryConfig selectByUserId(@Param("userId") Long userId);

    /**
     * 插入薪资配置
     *
     * @param config 薪资配置
     * @return 影响行数
     */
    int insert(UserSalaryConfig config);

    /**
     * 更新薪资配置
     *
     * @param config 薪资配置
     * @return 影响行数
     */
    int update(UserSalaryConfig config);

    /**
     * 根据用户ID删除配置
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteByUserId(@Param("userId") Long userId);
}
