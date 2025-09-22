package com.xiaou.moyu.mapper;

import com.xiaou.moyu.domain.UserCalendarCollection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户日历收藏数据访问层
 *
 * @author xiaou
 */
@Mapper
public interface UserCalendarCollectionMapper {

    /**
     * 根据用户ID和收藏类型查询收藏列表
     *
     * @param userId 用户ID
     * @param collectionType 收藏类型
     * @param status 状态
     * @return 收藏列表
     */
    List<UserCalendarCollection> selectByUserIdAndType(@Param("userId") Long userId,
                                                       @Param("collectionType") Integer collectionType,
                                                       @Param("status") Integer status);

    /**
     * 查询用户是否已收藏指定目标
     *
     * @param userId 用户ID
     * @param collectionType 收藏类型
     * @param targetId 目标ID
     * @return 收藏记录
     */
    UserCalendarCollection selectByUserIdAndTarget(@Param("userId") Long userId,
                                                   @Param("collectionType") Integer collectionType,
                                                   @Param("targetId") Long targetId);

    /**
     * 插入收藏记录
     *
     * @param collection 收藏记录
     * @return 影响行数
     */
    int insert(UserCalendarCollection collection);

    /**
     * 更新收藏状态
     *
     * @param userId 用户ID
     * @param collectionType 收藏类型
     * @param targetId 目标ID
     * @param status 状态
     * @return 影响行数
     */
    int updateStatus(@Param("userId") Long userId,
                     @Param("collectionType") Integer collectionType,
                     @Param("targetId") Long targetId,
                     @Param("status") Integer status);

    /**
     * 根据用户ID删除收藏记录
     *
     * @param userId 用户ID
     * @param collectionType 收藏类型
     * @param targetId 目标ID
     * @return 影响行数
     */
    int deleteByUserIdAndTarget(@Param("userId") Long userId,
                                @Param("collectionType") Integer collectionType,
                                @Param("targetId") Long targetId);

    /**
     * 统计用户收藏数量
     *
     * @param userId 用户ID
     * @param collectionType 收藏类型
     * @param status 状态
     * @return 收藏数量
     */
    Long countByUserIdAndType(@Param("userId") Long userId,
                              @Param("collectionType") Integer collectionType,
                              @Param("status") Integer status);
}
