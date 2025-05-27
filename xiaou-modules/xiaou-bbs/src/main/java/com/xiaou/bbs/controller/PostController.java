package com.xiaou.bbs.controller;


import cn.dev33.satoken.stp.StpUtil;
import com.xiaou.bbs.domain.bo.PostBo;
import com.xiaou.bbs.domain.vo.PostVo;
import com.xiaou.bbs.serivce.PostService;
import com.xiaou.common.domain.R;
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
        return postService.edit(id,postBo);
    }

}
