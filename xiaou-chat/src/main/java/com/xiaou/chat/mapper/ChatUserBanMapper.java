package com.xiaou.chat.mapper;

import com.xiaou.chat.domain.ChatUserBan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户禁言Mapper
 * 
 * @author xiaou
 */
@Mapper
public interface ChatUserBanMapper {
    
    /**
     * 插入禁言记录
     */
    int insert(ChatUserBan ban);
    
    /**
     * 查询用户是否被禁言
     */
    ChatUserBan selectActiveByUserId(@Param("userId") Long userId, @Param("roomId") Long roomId);
    
    /**
     * 解除禁言
     */
    int unban(Long id);
    
    /**
     * 查询过期的禁言记录
     */
    List<ChatUserBan> selectExpiredBans();
    
    /**
     * 批量解除过期禁言
     */
    int batchUnban(@Param("ids") List<Long> ids);
}
