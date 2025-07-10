package com.xiaou.bbs.controller;

import com.xiaou.bbs.domain.req.BbsCommentReplyReq;
import com.xiaou.bbs.domain.req.BbsCommentReq;
import com.xiaou.bbs.domain.resp.BbsCommentReplyResp;
import com.xiaou.bbs.domain.resp.BbsCommentResp;
import com.xiaou.bbs.service.BbsCommentReplyService;
import com.xiaou.bbs.service.BbsCommentService;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/post/comment")
@Validated
public class PostCommentController {
    @Resource
    private BbsCommentService commentService;
    @Resource
    private BbsCommentReplyService commentReplyService;
    /**
     * 添加一个评论，就是一级评论
     */
    @PostMapping("/add")
    public R<String> addComment(@RequestBody BbsCommentReq req){
        return commentService.addComment(req);
    }
    /**
     * 回复一个评论
     */
    @PostMapping("/reply")
    public R<String> replyComment(@RequestBody BbsCommentReplyReq req){
        return commentReplyService.replyComment(req);
    }
    /**
     * 删除一个评论
     */
    @PostMapping("/delete")
    public R<String> deleteComment(@RequestParam String id){
        return commentService.deleteComment(id);
    }
    /**
     * 删除一个回复
     */
    @PostMapping("/deleteReply")
    public R<String> deleteReply(@RequestParam String id){
        return commentReplyService.deleteReply(id);
    }
    /**
     * 获取一个帖子下的所有评论分页
     */
    @PostMapping("/getCommentList")
    public R<PageRespDto<BbsCommentResp>> getCommentList(@RequestParam String postId, @RequestBody PageReqDto dto){
        return commentService.getCommentList(postId,dto);
    }
    /**
     * 获取某个评论下的回复分页
     */
    @PostMapping("/getReplyList")
    public R<PageRespDto<BbsCommentReplyResp>> getReplyList(@RequestParam String commentId, @RequestBody PageReqDto dto){
        return commentReplyService.getReplyList(commentId,dto);
    }
    /**
     * 帖子评论点赞功能
     */
    @PostMapping("/like")
    public R<String> likeComment(@RequestParam String id){
        return commentService.likeComment(id);
    }
    /**
     * 帖子回复点赞功能
     */
    @PostMapping("/likeReply")
    public R<String> likeReply(@RequestParam String id){
        return commentReplyService.likeReply(id);
    }


}
