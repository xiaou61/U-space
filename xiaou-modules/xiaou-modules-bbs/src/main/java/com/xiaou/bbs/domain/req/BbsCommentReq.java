package com.xiaou.bbs.domain.req;

import com.xiaou.bbs.domain.entity.BbsComment;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

@Data
@AutoMapper(target = BbsComment.class)
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
