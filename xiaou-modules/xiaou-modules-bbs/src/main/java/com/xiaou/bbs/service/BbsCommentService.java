package com.xiaou.bbs.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.bbs.domain.entity.BbsComment;
import com.xiaou.bbs.domain.req.BbsCommentReq;
import com.xiaou.bbs.domain.resp.BbsCommentResp;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;

public interface BbsCommentService extends IService<BbsComment> {

    R<String> addComment(BbsCommentReq req);


    R<String> deleteComment(String id);

    R<PageRespDto<BbsCommentResp>> getCommentList(String postId, PageReqDto dto);

    R<String> likeComment(String id);
}
