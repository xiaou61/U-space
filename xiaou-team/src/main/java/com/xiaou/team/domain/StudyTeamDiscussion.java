package com.xiaou.team.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小组讨论实体类
 * 
 * @author xiaou
 */
@Data
public class StudyTeamDiscussion {
    
    /**
     * 讨论ID
     */
    private Long id;
    
    /**
     * 小组ID
     */
    private Long teamId;
    
    /**
     * 发布者ID
     */
    private Long userId;
    
    /**
     * 分类：1公告 2问题 3笔记 4经验 5闲聊
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
     * 图片URL
     */
    private String images;
    
    /**
     * 是否置顶
     */
    private Integer isPinned;
    
    // isTop 别名，兼容Service使用
    public Integer getIsTop() {
        return isPinned;
    }
    
    public void setIsTop(Integer isTop) {
        this.isPinned = isTop;
    }
    
    /**
     * 是否精华
     */
    private Integer isEssence;
    
    /**
     * 浏览数
     */
    private Integer viewCount;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 评论数
     */
    private Integer commentCount;
    
    /**
     * 状态：0已删除 1正常
     */
    private Integer status;
    
    /**
     * 是否删除：0否 1是
     */
    private Integer isDeleted;
    
    /**
     * 创建人
     */
    private Long createBy;
    
    /**
     * 发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    
    /**
     * 更新人
     */
    private Long updateBy;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
    
    /**
     * 最后回复时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastReplyTime;
}
