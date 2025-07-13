package com.xiaou.bbs.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 帖子推荐得分表
 *
 * @TableName u_post_recommend_score
 */
@TableName(value = "u_post_recommend_score")
@Data
public class PostRecommendScore {
    /**
     *
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 帖子ID
     */
    private String postId;

    /**
     * 热度分
     */
    private Double heatScore;

    /**
     * 停留时长分
     */
    private Double stayScore;

    /**
     * 关键词匹配分
     */
    private Double keywordScore;

    /**
     * 最终推荐得分
     */
    private Double finalScore;

    /**
     *
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}