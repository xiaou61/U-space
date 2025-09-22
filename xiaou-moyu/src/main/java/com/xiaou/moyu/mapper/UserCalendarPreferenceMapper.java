package com.xiaou.moyu.mapper;

import com.xiaou.moyu.domain.UserCalendarPreference;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户日历偏好设置数据访问层
 *
 * @author xiaou
 */
@Mapper
public interface UserCalendarPreferenceMapper {

    /**
     * 根据用户ID查询偏好设置
     *
     * @param userId 用户ID
     * @return 偏好设置
     */
    UserCalendarPreference selectByUserId(@Param("userId") Long userId);

    /**
     * 插入偏好设置
     *
     * @param preference 偏好设置
     * @return 影响行数
     */
    int insert(UserCalendarPreference preference);

    /**
     * 根据用户ID更新偏好设置
     *
     * @param preference 偏好设置
     * @return 影响行数
     */
    int updateByUserId(UserCalendarPreference preference);

    /**
     * 根据用户ID删除偏好设置
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteByUserId(@Param("userId") Long userId);
}
