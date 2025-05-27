package com.xiaou.bbs.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;


@TableName(value ="u_post_comment_like")
@Data
public class PostCommentLike {
    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 评论ID
     */
    private Long commentId;

    /**
     * 点赞时间
     */
    private Date createTime;
}