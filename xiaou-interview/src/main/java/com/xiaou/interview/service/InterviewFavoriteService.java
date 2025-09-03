package com.xiaou.interview.service;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.interview.domain.InterviewFavorite;

import java.util.List;

/**
 * 面试收藏服务接口
 *
 * @author xiaou
 */
public interface InterviewFavoriteService {

    /**
     * 添加收藏
     */
    void addFavorite(Long userId, Integer targetType, Long targetId);

    /**
     * 取消收藏
     */
    void removeFavorite(Long userId, Integer targetType, Long targetId);

    /**
     * 检查是否已收藏
     */
    boolean isFavorited(Long userId, Integer targetType, Long targetId);

    /**
     * 获取用户收藏列表
     */
    List<InterviewFavorite> getUserFavorites(Long userId, Integer targetType);

    /**
     * 分页获取用户收藏列表
     */
    PageResult<InterviewFavorite> getUserFavoritePage(Long userId, Integer targetType, int page, int size);

    /**
     * 获取收藏统计
     */
    int getFavoriteCount(Integer targetType, Long targetId);

    /**
     * 批量删除收藏
     */
    void batchRemoveFavorites(List<Long> ids);
} 