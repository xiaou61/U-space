package com.xiaou.community.controller.pub;

import com.xiaou.common.annotation.Log;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.core.domain.Result;
import com.xiaou.community.domain.CommunityTag;
import com.xiaou.community.dto.CommunityPostQueryRequest;
import com.xiaou.community.dto.CommunityPostResponse;
import com.xiaou.community.service.CommunityPostService;
import com.xiaou.community.service.CommunityTagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前台标签控制器
 * 
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/community/tags")
@RequiredArgsConstructor
public class CommunityTagController {
    
    private final CommunityTagService communityTagService;
    private final CommunityPostService communityPostService;
    
    /**
     * 获取启用的标签列表
     */
    @Log(module = "社区", type = Log.OperationType.SELECT, description = "查询标签列表")
    @GetMapping
    public Result<List<CommunityTag>> getEnabledTags() {
        List<CommunityTag> tags = communityTagService.getEnabledList();
        return Result.success(tags);
    }
    
    /**
     * 获取热门标签列表
     */
    @Log(module = "社区", type = Log.OperationType.SELECT, description = "查询热门标签")
    @GetMapping("/hot")
    public Result<List<CommunityTag>> getHotTags(@RequestParam(defaultValue = "10") Integer limit) {
        List<CommunityTag> tags = communityTagService.getHotList(limit);
        return Result.success(tags);
    }
    
    /**
     * 获取指定标签下的帖子列表
     */
    @Log(module = "社区", type = Log.OperationType.SELECT, description = "查询标签帖子列表")
    @PostMapping("/{id}/posts")
    public Result<PageResult<CommunityPostResponse>> getTagPosts(
            @PathVariable Long id,
            @RequestBody CommunityPostQueryRequest request) {
        // 设置标签ID
        request.setTagId(id);
        PageResult<CommunityPostResponse> result = communityPostService.getPostList(request);
        return Result.success(result);
    }
}

