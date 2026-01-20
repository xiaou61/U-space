package com.xiaou.ai.service;

import com.xiaou.ai.dto.community.PostSummaryResult;

/**
 * 社区AI服务接口
 *
 * @author xiaou
 */
public interface AiCommunityService {

    /**
     * 生成帖子AI摘要
     *
     * @param title   帖子标题
     * @param content 帖子内容
     * @return 摘要结果
     */
    PostSummaryResult generatePostSummary(String title, String content);
}
