package com.xiaou.bbs.domain.req;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaou.bbs.domain.entity.BbsPost;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 帖子表：记录校园BBS的帖子内容，关联帖子分类
 * @TableName u_bbs_post
 */
@Data
@AutoMapper(target = BbsPost.class)
public class BbsPostReq {


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

}