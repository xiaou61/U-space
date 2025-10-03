package com.xiaou.community.controller.pub;

import com.xiaou.common.annotation.Log;
import com.xiaou.common.core.domain.Result;
import com.xiaou.community.domain.CommunityUserStatus;
import com.xiaou.community.service.CommunityCacheService;
import com.xiaou.community.service.CommunityUserStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 社区公共接口控制器
 * 
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/community")
@RequiredArgsConstructor
public class CommunityPublicController {
    
    private final CommunityUserStatusService communityUserStatusService;
    private final CommunityCacheService communityCacheService;
    
    /**
     * 用户进入社区时的初始化接口
     * 检测用户是否在社区用户表中，如果不存在则自动创建
     */
    @Log(module = "社区", type = Log.OperationType.SELECT, description = "用户进入社区初始化")
    @GetMapping("/init")
    public Result<CommunityUserStatus> initUser() {
        CommunityUserStatus userStatus = communityUserStatusService.getCurrentUserStatus();
        return Result.success(userStatus);
    }
    
    /**
     * 获取当前用户社区状态
     */
    @GetMapping("/user/status")
    public Result<CommunityUserStatus> getCurrentUserStatus() {
        CommunityUserStatus userStatus = communityUserStatusService.getCurrentUserStatus();
        return Result.success(userStatus);
    }
    
    /**
     * 获取热门搜索词
     */
    @Log(module = "社区", type = Log.OperationType.SELECT, description = "获取热门搜索词")
    @GetMapping("/hot-keywords")
    public Result<List<String>> getHotKeywords(@RequestParam(defaultValue = "10") Integer limit) {
        List<String> keywords = communityCacheService.getHotSearchKeywords(limit);
        return Result.success(keywords);
    }
} 