package com.xiaou.bbs.domain.req;

import lombok.Data;

@Data
public class BbsCommentReq {
    /**
     * 所属帖子ID
     */
    private String postId;


    /**
     * 评论内容
     */
    private String content;
}
