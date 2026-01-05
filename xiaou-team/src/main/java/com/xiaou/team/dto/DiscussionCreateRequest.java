package com.xiaou.team.dto;

import lombok.Data;

import java.util.List;

/**
 * 创建讨论请求DTO
 * 
 * @author xiaou
 */
@Data
public class DiscussionCreateRequest {
    
    /**
     * 分类：1公告 2问答 3笔记 4经验 5闲聊
     */
    private Integer category;
    
    /**
     * 标题
     */
    private String title;
    
    /**
     * 内容
     */
    private String content;
    
    /**
     * 图片列表
     */
    private List<String> images;
    
    /**
     * 是否置顶
     */
    private Integer isTop;
    
    /**
     * 是否精华
     */
    private Integer isEssence;
}
