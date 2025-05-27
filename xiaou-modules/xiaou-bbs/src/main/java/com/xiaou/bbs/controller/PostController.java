package com.xiaou.bbs.controller;


import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.xiaou.bbs.domain.bo.PostBo;
import com.xiaou.bbs.domain.vo.PostVo;
import com.xiaou.bbs.serivce.PostService;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.log.annotation.Log;
import com.xiaou.log.enums.BusinessType;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/post")
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
    @PostMapping("/get/{id}")
    public R<PostVo> get(@PathVariable Long id) {
        return postService.get(id);
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
}
