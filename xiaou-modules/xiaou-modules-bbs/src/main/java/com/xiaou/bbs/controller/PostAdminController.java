package com.xiaou.bbs.controller;

import com.xiaou.bbs.service.BbsPostService;
import com.xiaou.common.domain.R;
import com.xiaou.log.annotation.Log;
import com.xiaou.log.enums.BusinessType;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/post")
@Validated
public class PostAdminController {
    @Resource
    private BbsPostService postService;
    /**
     * 删除帖子
     */
    @Log(title = "帖子删除", businessType = BusinessType.DELETE)
    @PostMapping("/delete")
    public R<String> delete(@RequestParam String id) {
        return postService.deleteAdmin(id);
    }

}
