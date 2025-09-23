package com.xiaou.moyu.mapper;

import com.xiaou.moyu.domain.UserBugHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户Bug浏览历史Mapper接口
 *
 * @author xiaou
 */
@Mapper
public interface UserBugHistoryMapper {
    
    /**
     * 插入浏览历史
     */
    int insert(UserBugHistory history);
    
    /**
     * 查询用户最近浏览的Bug ID列表
     */
    List<Long> selectRecentBugIds(@Param("userId") Long userId, @Param("hours") Integer hours, @Param("limit") Integer limit);
    
    /**
     * 删除过期的浏览历史
     */
    int deleteExpiredHistory(@Param("beforeTime") LocalDateTime beforeTime);
    
    /**
     * 查询用户是否浏览过指定Bug
     */
    boolean existsByUserIdAndBugId(@Param("userId") Long userId, @Param("bugId") Long bugId, @Param("hours") Integer hours);
}
