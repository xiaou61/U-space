package com.xiaou.bbs.controller;


import cn.dev33.satoken.stp.StpUtil;
import com.xiaou.bbs.domain.bo.PostBo;
import com.xiaou.bbs.domain.dto.PostUpdateCountReqDto;
import com.xiaou.bbs.domain.page.CategoryPageReqDto;
import com.xiaou.bbs.domain.vo.PostVo;
import com.xiaou.bbs.serivce.PostService;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.log.annotation.Log;
import com.xiaou.log.enums.BusinessType;
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
     * 编辑帖子
     */
    @PutMapping("/edit/{id}")
    public R<String> edit(@PathVariable Long id, @RequestBody PostBo postBo) {
        return postService.edit(id, postBo);
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
     * 封禁帖子目前只有管理员可以
     */
    @DeleteMapping("/admin/ban/{id}")
    @Log(title = "封禁管理", businessType = BusinessType.OTHER)
    public R<String> adminDelete(@PathVariable Long id) {
        if (StpUtil.hasRole("admin")) {
            return postService.banAdmin(id);
        }
        return R.fail("权限不足");
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

}
