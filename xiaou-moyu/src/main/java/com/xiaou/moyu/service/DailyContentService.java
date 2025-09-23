package com.xiaou.moyu.service;

import com.xiaou.moyu.domain.DailyContent;

import java.util.List;

/**
 * 每日内容服务接口
 *
 * @author xiaou
 */
public interface DailyContentService {

    /**
     * 根据内容类型获取内容列表
     *
     * @param contentType 内容类型
     * @param limit 限制数量
     * @return 内容列表
     */
    List<DailyContent> getContentByType(Integer contentType, Integer limit);

    /**
     * 随机获取指定类型的内容
     *
     * @param contentType 内容类型
     * @param limit 限制数量
     * @return 内容列表
     */
    List<DailyContent> getRandomContentByType(Integer contentType, Integer limit);

    /**
     * 根据用户偏好获取推荐内容
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 推荐内容列表
     */
    List<DailyContent> getRecommendedContent(Long userId, Integer limit);

    /**
     * 获取今日推荐内容
     *
     * @param userId 用户ID
     * @return 今日推荐内容
     */
    List<DailyContent> getTodayContent(Long userId);

    /**
     * 获取热门内容
     *
     * @param limit 限制数量
     * @return 热门内容列表
     */
    List<DailyContent> getPopularContent(Integer limit);

    /**
     * 根据编程语言获取内容
     *
     * @param programmingLanguage 编程语言
     * @param limit 限制数量
     * @return 内容列表
     */
    List<DailyContent> getContentByLanguage(String programmingLanguage, Integer limit);

    /**
     * 增加内容查看次数
     *
     * @param contentId 内容ID
     * @return 是否成功
     */
    boolean incrementViewCount(Long contentId);

    /**
     * 点赞内容
     *
     * @param contentId 内容ID
     * @return 是否成功
     */
    boolean likeContent(Long contentId);

    /**
     * 切换内容收藏状态
     *
     * @param userId 用户ID
     * @param contentId 内容ID
     * @return 是否成功
     */
    boolean toggleContentCollection(Long userId, Long contentId);

    /**
     * 获取用户收藏的内容列表
     *
     * @param userId 用户ID
     * @return 收藏的内容列表
     */
    List<DailyContent> getUserCollectedContent(Long userId);

    /**
     * 检查用户是否收藏了指定内容
     *
     * @param userId 用户ID
     * @param contentId 内容ID
     * @return 是否已收藏
     */
    boolean isContentCollected(Long userId, Long contentId);

    // ========== 后台管理方法 ==========

    /**
     * 获取所有内容（后台管理）
     *
     * @param contentType 内容类型
     * @param limit 限制数量
     * @return 所有内容列表
     */
    List<DailyContent> getAllContent(Integer contentType, Integer limit);

    /**
     * 根据类型获取内容（后台管理）
     *
     * @param contentType 内容类型
     * @param limit 限制数量
     * @return 内容列表
     */
    List<DailyContent> getAdminContentByType(Integer contentType, Integer limit);

    /**
     * 根据ID获取内容（后台管理）
     *
     * @param id 内容ID
     * @return 内容信息
     */
    DailyContent getContentById(Long id);

    /**
     * 创建内容（后台管理）
     *
     * @param content 内容信息
     * @return 是否成功
     */
    boolean createContent(DailyContent content);

    /**
     * 更新内容（后台管理）
     *
     * @param content 内容信息
     * @return 是否成功
     */
    boolean updateContent(DailyContent content);

    /**
     * 删除内容（后台管理）
     *
     * @param id 内容ID
     * @return 是否成功
     */
    boolean deleteContent(Long id);

    /**
     * 批量删除内容（后台管理）
     *
     * @param ids 内容ID列表
     * @return 是否成功
     */
    boolean batchDeleteContent(List<Long> ids);

    /**
     * 更新内容状态（后台管理）
     *
     * @param id 内容ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateContentStatus(Long id, Integer status);

    /**
     * 获取内容统计信息（后台管理）
     *
     * @return 统计信息
     */
    java.util.Map<String, Object> getContentStatistics();

    /**
     * 获取收藏统计信息（后台管理）
     *
     * @return 收藏统计信息
     */
    java.util.Map<String, Object> getCollectionStatistics();
}
