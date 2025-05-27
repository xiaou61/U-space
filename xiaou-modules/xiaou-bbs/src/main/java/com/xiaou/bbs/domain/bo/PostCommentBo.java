package com.xiaou.bbs.domain.bo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaou.bbs.domain.entity.PostComment;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.util.Date;

@Data
@AutoMapper(target = PostComment.class)
public class PostCommentBo {



    /**
     * 帖子ID
     */
    private Long postId;


    /**
     * 父评论ID（0表示一级评论）
     */
    private Long parentId;

    /**
     * 评论内容
     */
    private String content;

}