package com.xiaou.bbs.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.bbs.domain.entity.BbsCommentReply;
import com.xiaou.bbs.domain.req.BbsCommentReplyReq;
import com.xiaou.bbs.domain.resp.BbsCommentReplyResp;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;

public interface BbsCommentReplyService extends IService<BbsCommentReply> {

    R<String> replyComment(BbsCommentReplyReq req);

    R<String> deleteReply(String id);

    R<PageRespDto<BbsCommentReplyResp>> getReplyList(String commentId, PageReqDto dto);
}
