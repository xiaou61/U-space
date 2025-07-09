package com.xiaou.bbs.domain.dto;

import lombok.Data;

@Data
public class CommentReplyCount {
    private String commentId;
    private Long replyCount;
}
