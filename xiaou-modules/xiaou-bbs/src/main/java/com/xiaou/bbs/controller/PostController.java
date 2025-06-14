package com.xiaou.bbs.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiaou.bbs.domain.bo.PostBo;
import com.xiaou.bbs.domain.dto.PostUpdateCountReqDto;
import com.xiaou.bbs.domain.entity.Post;
import com.xiaou.bbs.domain.page.CategoryPageReqDto;
import com.xiaou.bbs.domain.vo.PostLikeInfoVo;
import com.xiaou.bbs.domain.vo.PostVo;
import com.xiaou.bbs.manager.PostLikeManager;
import com.xiaou.bbs.serivce.PostService;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.utils.LoginHelper;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/post")
@Validated
public class PostController {

    @Resource
    private PostService postService;



    /**
     * 创建帖子
     */
    @PostMapping("/create")
    public R<String> create(@RequestBody PostBo postBo) {
        return postService.create(postBo);
    }

    /**
     * 获取帖子详情
     */
    @PostMapping("/get/{postId}")
    public R<PostVo> get(@PathVariable Long postId) {
        // 增加浏览量
        postService.addViewCount(postId);
        //获得信息
        return postService.get(postId);
    }

    /**
     * 删除帖子
     */
    @DeleteMapping("/delete/{id}")
    public R<String> delete(@PathVariable Long id) {
        return postService.delete(id);
    }


    /**
     * 分页查看帖子
     */
    @PostMapping("/list")
    public R<PageRespDto<PostVo>> list(@RequestBody PageReqDto dto) {
        return postService.allPostPage(dto);
    }

    /**
     * 分页查看指定分类帖子
     */
    @PostMapping("/listByCategory")
    public R<PageRespDto<PostVo>> listByCategory(@RequestBody CategoryPageReqDto dto) {
        return postService.pageByCategory(dto);
    }


    /**
     * 帖子点赞 传入post_id
     */
    @PostMapping("/like/{postId}")
    public R<String> toggleLike(@PathVariable Long postId) {
        return postService.toggleLike(postId);
    }


    /**
     * 帖子搜索
     *
     * @param keyword
     * @return
     */
    @GetMapping("/search")
    public R<List<PostVo>> search(@RequestParam String keyword) {
        return R.ok(postService.searchPosts(keyword));
    }

    /**
     * 刷新帖子后返回新增的帖子数量
     *
     * @param dto
     * @return
     */
    @PostMapping("/countNewPosts")
    public R<Long> countNewPosts(@RequestBody PostUpdateCountReqDto dto) {
        Long count = postService.countNewPostsSince(dto.getLastRefreshTime());
        return R.ok(count);
    }


    /**
     * 查询当前用户点赞过的帖子列表
     */
    @GetMapping("/liked-posts")
    public R<List<PostLikeInfoVo>> getLikedPosts() {
        return postService.getCurrentUserLikedPosts();
    }

    /**
     * 个人帖子总数
     */
    @GetMapping("/count")
    public R<Long> count() {
        return R.ok(postService.count(new QueryWrapper<Post>().eq("user_id",  LoginHelper.getCurrentAppUserId())));
    }
    /**
     * 个人点赞帖子总数
     */
    @GetMapping("/count-liked")
     public R<Long> countLiked() {
        return R.ok(postService.countLiked());
    }
}
