package com.xiaou.bbs.domain.req;

import com.xiaou.bbs.domain.entity.BbsCommentReply;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

@Data
@AutoMapper(target = BbsCommentReply.class)
public class BbsCommentReplyReq {
    /**
     * 所属评论ID
     */
    private String commentId;

    /**
     * 所属帖子ID，便于反查
     */
    private String postId;


    /**
     * 被回复的用户ID（@）
     */
    private String replyUserId;

    /**
     * 回复内容
     */
    private String content;
}
