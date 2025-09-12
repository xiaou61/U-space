package com.xiaou.sensitive.controller.api;

import com.xiaou.common.core.domain.Result;
import com.xiaou.sensitive.api.dto.SensitiveCheckRequest;
import com.xiaou.sensitive.api.dto.SensitiveCheckResponse;
import com.xiaou.sensitive.api.SensitiveCheckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 敏感词检测API控制器
 */
@Slf4j
@RestController
@RequestMapping("/sensitive")
@RequiredArgsConstructor
public class SensitiveWordController {

    private final SensitiveCheckService sensitiveCheckService;

    /**
     * 检测文本中的敏感词
     */
    @PostMapping("/check")
    public Result<SensitiveCheckResponse> checkText(@RequestBody SensitiveCheckRequest request) {
        try {
            SensitiveCheckResponse response = sensitiveCheckService.checkText(request);
            return Result.success(response);
        } catch (Exception e) {
            log.error("敏感词检测失败：{}", e.getMessage(), e);
            return Result.error("敏感词检测失败");
        }
    }

    /**
     * 批量检测文本中的敏感词
     */
    @PostMapping("/check/batch")
    public Result<List<SensitiveCheckResponse>> checkTextBatch(@RequestBody List<SensitiveCheckRequest> requests) {
        try {
            List<SensitiveCheckResponse> responses = sensitiveCheckService.checkTextBatch(requests);
            return Result.success(responses);
        } catch (Exception e) {
            log.error("批量敏感词检测失败：{}", e.getMessage(), e);
            return Result.error("批量敏感词检测失败");
        }
    }
} 