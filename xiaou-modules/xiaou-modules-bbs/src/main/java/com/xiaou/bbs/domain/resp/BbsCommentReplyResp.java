package com.xiaou.bbs.domain.resp;

import com.xiaou.bbs.domain.entity.BbsCommentReply;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

@Data
@AutoMapper(target = BbsCommentReply.class)
public class BbsCommentReplyResp {

    private String id;

    /**
     * 所属评论ID
     */
    private String commentId;

    /**
     * 所属帖子ID，便于反查
     */
    private String postId;

    /**
     * 回复人ID
     */
    private String userId;

    /**
     * 被回复的用户ID（@）
     */
    private String replyUserId;

    /**
     * 回复内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 是否是自己的回复
     */
    private Boolean isMine;
    /**================回复人的信息=====================*/

    private String replyUserName;
    private String replyUserAvatar;
    /**================被回复人的信息=====================*/
    private String replyToUserName;
    private String replyToUserAvatar;



}
