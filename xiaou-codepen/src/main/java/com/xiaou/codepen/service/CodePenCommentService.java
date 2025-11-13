package com.xiaou.codepen.service;

import com.xiaou.codepen.domain.CodePenComment;
import com.xiaou.codepen.dto.CommentCreateRequest;

import java.util.List;

/**
 * 作品评论Service
 * 
 * @author xiaou
 */
public interface CodePenCommentService {
    
    /**
     * 发表评论
     */
    Long createComment(CommentCreateRequest request, Long userId);
    
    /**
     * 删除评论
     */
    boolean deleteComment(Long id, Long userId);
    
    /**
     * 获取作品的评论列表
     */
    List<CodePenComment> getCommentList(Long penId);
    
    /**
     * 隐藏评论（管理员）
     */
    boolean hideComment(Long id);
    
    /**
     * 管理员删除评论
     */
    boolean adminDeleteComment(Long id);
}

