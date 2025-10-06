package com.xiaou.community.controller.pub;

import com.xiaou.common.annotation.Log;
import com.xiaou.common.core.domain.Result;
import com.xiaou.community.service.CommunityAiSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 社区AI功能控制器
 * 
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/community/posts")
@RequiredArgsConstructor
public class CommunityAiController {
    
    private final CommunityAiSummaryService communityAiSummaryService;
    
    /**
     * 生成AI摘要
     */
    @Log(module = "社区", type = Log.OperationType.UPDATE, description = "生成AI摘要")
    @PostMapping("/{id}/generate-summary")
    public Result<Map<String, Object>> generateSummary(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") Boolean forceRefresh) {
        Map<String, Object> result = communityAiSummaryService.generateSummary(id, forceRefresh);
        return Result.success(result);
    }
    
    /**
     * 获取帖子摘要
     */
    @Log(module = "社区", type = Log.OperationType.SELECT, description = "查询帖子摘要")
    @GetMapping("/{id}/summary")
    public Result<Map<String, Object>> getSummary(@PathVariable Long id) {
        Map<String, Object> result = communityAiSummaryService.getSummary(id);
        return Result.success(result);
    }
}

