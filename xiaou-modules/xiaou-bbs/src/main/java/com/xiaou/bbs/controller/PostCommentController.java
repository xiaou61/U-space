package com.xiaou.bbs.controller;

import com.xiaou.bbs.domain.bo.PostCommentBo;
import com.xiaou.bbs.domain.vo.PostCommentPageVo;
import com.xiaou.bbs.domain.vo.PostCommentVo;
import com.xiaou.bbs.serivce.PostCommentService;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/post/common")
@Validated
public class PostCommentController {
    @Resource
    private PostCommentService postCommentService;

    /**
     * 创建帖子评论
     */
    @PostMapping("/create")
    public R<String> create(@RequestBody PostCommentBo bo) {
        return postCommentService.create(bo);
    }

    /**
     * 删除帖子评论
     */
    @DeleteMapping("/delete/{id}")
    public R<String> delete(@PathVariable Long id) {
        return postCommentService.delete(id);
    }

    /**
     * 分页查看帖子评论
     */
    @PostMapping("/page")
    public R<PageRespDto<PostCommentPageVo>> page(@RequestBody PageReqDto dto) {
        return postCommentService.allPostCommentPage(dto);
    }
    /**
     * 根据id分页查看帖子评论
     */
     @PostMapping("/pageById/{id}")
     public R<PageRespDto<PostCommentPageVo>> pageById(@RequestBody PageReqDto dto,@PathVariable Long id) {
         return postCommentService.pageById(dto,id);
     }

    /**
     * 切换帖子评论的点赞状态
     */
    @PostMapping("/like/{commentId}")
    public R<String> toggleCommentLike(@PathVariable Long commentId) {
        return postCommentService.toggleCommentLike(commentId);
    }

}
