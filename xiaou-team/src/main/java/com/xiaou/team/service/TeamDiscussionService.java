package com.xiaou.team.service;

import com.xiaou.team.dto.DiscussionCreateRequest;
import com.xiaou.team.dto.DiscussionResponse;

import java.util.List;

/**
 * 讨论服务接口
 * 
 * @author xiaou
 */
public interface TeamDiscussionService {
    
    /**
     * 创建讨论
     * 
     * @param teamId 小组ID
     * @param request 创建请求
     * @param userId 用户ID
     * @return 讨论ID
     */
    Long createDiscussion(Long teamId, DiscussionCreateRequest request, Long userId);
    
    /**
     * 更新讨论
     * 
     * @param discussionId 讨论ID
     * @param request 更新请求
     * @param userId 用户ID
     */
    void updateDiscussion(Long discussionId, DiscussionCreateRequest request, Long userId);
    
    /**
     * 删除讨论
     * 
     * @param discussionId 讨论ID
     * @param userId 用户ID
     */
    void deleteDiscussion(Long discussionId, Long userId);
    
    /**
     * 获取讨论详情
     * 
     * @param discussionId 讨论ID
     * @param userId 当前用户ID
     * @return 讨论详情
     */
    DiscussionResponse getDiscussionDetail(Long discussionId, Long userId);
    
    /**
     * 获取讨论列表
     * 
     * @param teamId 小组ID
     * @param category 分类（可选）
     * @param keyword 关键词（可选）
     * @param page 页码
     * @param pageSize 每页条数
     * @param userId 当前用户ID
     * @return 讨论列表
     */
    List<DiscussionResponse> getDiscussionList(Long teamId, Integer category, String keyword, 
                                                Integer page, Integer pageSize, Long userId);
    
    /**
     * 置顶/取消置顶
     * 
     * @param discussionId 讨论ID
     * @param isTop 是否置顶
     * @param userId 操作人ID
     */
    void setTop(Long discussionId, Integer isTop, Long userId);
    
    /**
     * 设为精华/取消精华
     * 
     * @param discussionId 讨论ID
     * @param isEssence 是否精华
     * @param userId 操作人ID
     */
    void setEssence(Long discussionId, Integer isEssence, Long userId);
    
    /**
     * 点赞讨论
     * 
     * @param discussionId 讨论ID
     * @param userId 用户ID
     */
    void likeDiscussion(Long discussionId, Long userId);
    
    /**
     * 取消点赞
     * 
     * @param discussionId 讨论ID
     * @param userId 用户ID
     */
    void unlikeDiscussion(Long discussionId, Long userId);
}
