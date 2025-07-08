package com.xiaou.bbs.domain.resp;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 帖子表：记录校园BBS的帖子内容，关联帖子分类
 * @TableName u_bbs_post
 */
@Data
public class BbsPostResp {
    /**
     * 帖子ID，UUID
     */
    private String id;

    /**
     * 发布人ID，关联用户表
     */
    private String userId;

    //发布人的信息
    private BbsStudentInfoResp userInfo;

    /**
     * 帖子分类ID，关联 u_bbs_category 表
     */
    private String categoryId;

    /**
     * 帖子标题
     */
    private String title;

    /**
     * 帖子内容
     */
    private String content;

    /**
     * 帖子图片URL数组，JSON格式
     */
    private List<String> images;

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
     * 创建时间
     */
    private Date createdAt;

}