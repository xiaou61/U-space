package com.xiaou.team.mapper;

import com.xiaou.team.domain.StudyTeamCheckinLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 打卡点赞Mapper
 * 
 * @author xiaou
 */
@Mapper
public interface StudyTeamCheckinLikeMapper {
    
    /**
     * 插入点赞记录
     */
    int insert(StudyTeamCheckinLike like);
    
    /**
     * 删除点赞记录
     */
    int delete(@Param("checkinId") Long checkinId, @Param("userId") Long userId);
    
    /**
     * 检查用户是否已点赞
     */
    Integer checkExists(@Param("checkinId") Long checkinId, @Param("userId") Long userId);
    
    /**
     * 统计打卡点赞数
     */
    int countByCheckinId(@Param("checkinId") Long checkinId);
}
