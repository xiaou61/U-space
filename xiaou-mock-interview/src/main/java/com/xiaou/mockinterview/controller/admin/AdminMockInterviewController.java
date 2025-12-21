package com.xiaou.mockinterview.controller.admin;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.core.domain.Result;
import com.xiaou.common.utils.PageHelper;
import com.xiaou.mockinterview.domain.MockInterviewDirection;
import com.xiaou.mockinterview.domain.MockInterviewSession;
import com.xiaou.mockinterview.dto.request.InterviewHistoryRequest;
import com.xiaou.mockinterview.mapper.MockInterviewDirectionMapper;
import com.xiaou.mockinterview.mapper.MockInterviewSessionMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端模拟面试控制器
 *
 * @author xiaou
 */
@Tag(name = "模拟面试-管理端", description = "管理端模拟面试相关接口")
@RestController
@RequestMapping("/admin/mock-interview")
@RequiredArgsConstructor
public class AdminMockInterviewController {

    private final MockInterviewDirectionMapper directionMapper;
    private final MockInterviewSessionMapper sessionMapper;

    // =================== 方向配置管理 ===================

    @Operation(summary = "获取方向配置列表")
    @GetMapping("/directions")
    public Result<List<MockInterviewDirection>> getDirections() {
        List<MockInterviewDirection> directions = directionMapper.selectAll();
        return Result.success(directions);
    }

    @Operation(summary = "新增方向配置")
    @PostMapping("/directions")
    public Result<Long> addDirection(@RequestBody DirectionRequest request) {
        MockInterviewDirection direction = new MockInterviewDirection()
                .setDirectionCode(request.getDirectionCode())
                .setDirectionName(request.getDirectionName())
                .setIcon(request.getIcon())
                .setDescription(request.getDescription())
                .setCategoryIds(request.getCategoryIds())
                .setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .setStatus(1)
                .setCreateTime(LocalDateTime.now());
        directionMapper.insert(direction);
        return Result.success("新增成功", direction.getId());
    }

    @Operation(summary = "更新方向配置")
    @PutMapping("/directions/{id}")
    public Result<Void> updateDirection(@PathVariable Long id, @RequestBody DirectionRequest request) {
        MockInterviewDirection direction = directionMapper.selectById(id);
        if (direction == null) {
            return Result.error("方向配置不存在");
        }

        direction.setDirectionName(request.getDirectionName())
                .setIcon(request.getIcon())
                .setDescription(request.getDescription())
                .setCategoryIds(request.getCategoryIds())
                .setSortOrder(request.getSortOrder());
        directionMapper.updateById(direction);
        return Result.success();
    }

    @Operation(summary = "删除方向配置")
    @DeleteMapping("/directions/{id}")
    public Result<Void> deleteDirection(@PathVariable Long id) {
        directionMapper.deleteById(id);
        return Result.success();
    }

    @Operation(summary = "更新方向状态")
    @PutMapping("/directions/{id}/status")
    public Result<Void> updateDirectionStatus(@PathVariable Long id, @RequestParam Integer status) {
        directionMapper.updateStatus(id, status);
        return Result.success();
    }

    // =================== 面试记录管理 ===================

    @Operation(summary = "获取面试记录列表")
    @PostMapping("/sessions")
    public Result<PageResult<MockInterviewSession>> getSessions(@RequestBody(required = false) InterviewHistoryRequest request) {
        if (request == null) {
            request = new InterviewHistoryRequest();
        }
        InterviewHistoryRequest finalRequest = request;
        PageResult<MockInterviewSession> result = PageHelper.doPage(request.getPageNum(), request.getPageSize(),
                () -> sessionMapper.selectPageAll(finalRequest));
        return Result.success(result);
    }

    // =================== 内部类 ===================

    @Data
    public static class DirectionRequest {
        private String directionCode;
        private String directionName;
        private String icon;
        private String description;
        private String categoryIds;
        private Integer sortOrder;
    }
}
