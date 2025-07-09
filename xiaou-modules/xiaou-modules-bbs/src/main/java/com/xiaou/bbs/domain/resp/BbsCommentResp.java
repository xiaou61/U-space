package com.xiaou.bbs.domain.resp;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.xiaou.bbs.domain.entity.BbsComment;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

@Data
@AutoMapper(target = BbsComment.class)
public class BbsCommentResp {
    /**
     * 评论ID，UUID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 所属帖子ID
     */
    private String postId;

    /**
     * 评论用户ID
     */
    private String userId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Integer likeCount;


    /**==========关联用户字段===========**/
    /**
     * name
     */
    private String name;
    /**
     * 头像
     */
    private String avatar;

    /**
     * 是否是自己的评论
     */
    private Boolean isMine;
    /**
     * 回复数
     */
    private Long replyCount;
}
