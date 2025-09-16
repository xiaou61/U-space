package com.xiaou.knowledge.controller.admin;

import com.xiaou.common.annotation.RequireAdmin;
import com.xiaou.common.annotation.Log;
import com.xiaou.common.core.domain.Result;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.utils.UserContextUtil;
import com.xiaou.knowledge.domain.KnowledgeMap;
import com.xiaou.knowledge.dto.request.CreateKnowledgeMapRequest;
import com.xiaou.knowledge.dto.request.UpdateKnowledgeMapRequest;
import com.xiaou.knowledge.dto.request.KnowledgeMapQueryRequest;
import com.xiaou.knowledge.dto.response.KnowledgeMapListResponse;
import com.xiaou.knowledge.service.KnowledgeMapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 管理员端知识图谱控制器
 * 
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/admin/knowledge/maps")
@RequiredArgsConstructor
@Tag(name = "管理员端知识图谱管理", description = "管理员端知识图谱相关接口")
@Validated
public class AdminKnowledgeMapController {
    
    private final KnowledgeMapService knowledgeMapService;
    private final UserContextUtil userContextUtil;
    
    @Operation(summary = "获取知识图谱列表")
    @Log(module = "知识图谱", type = Log.OperationType.SELECT, description = "分页查询知识图谱列表")
    @PostMapping("/list")
    @RequireAdmin
    public Result<PageResult<KnowledgeMapListResponse>> getMapList(@RequestBody KnowledgeMapQueryRequest request) {
        log.info("Controller收到知识图谱列表查询请求: {}", request);
        PageResult<KnowledgeMapListResponse> result = knowledgeMapService.getPageList(request);
        log.info("Controller返回结果，total: {}", result.getTotal());
        return Result.success(result);
    }
    
    @Operation(summary = "根据ID获取知识图谱详情")
    @GetMapping("/{id}")
    @RequireAdmin
    public Result<KnowledgeMap> getMapById(
            @Parameter(description = "图谱ID") @PathVariable Long id) {
        
        KnowledgeMap knowledgeMap = knowledgeMapService.getById(id);
        return Result.success(knowledgeMap);
    }
    
    @Operation(summary = "创建知识图谱")
    @PostMapping
    @RequireAdmin
    public Result<Long> createMap(@Valid @RequestBody CreateKnowledgeMapRequest request) {
        Long userId = userContextUtil.getCurrentUserId();
        Long mapId = knowledgeMapService.createMap(request, userId);
        return Result.success(mapId);
    }
    
    @Operation(summary = "更新知识图谱")
    @PutMapping("/{id}")
    @RequireAdmin
    public Result<Boolean> updateMap(
            @Parameter(description = "图谱ID") @PathVariable Long id,
            @Valid @RequestBody UpdateKnowledgeMapRequest request) {
        
        boolean success = knowledgeMapService.updateMap(id, request);
        return Result.success(success);
    }
    
    @Operation(summary = "发布知识图谱")
    @PostMapping("/{id}/publish")
    @RequireAdmin
    public Result<Boolean> publishMap(
            @Parameter(description = "图谱ID") @PathVariable Long id) {
        
        boolean success = knowledgeMapService.publishMap(id);
        return Result.success(success);
    }
    
    @Operation(summary = "隐藏知识图谱")
    @PostMapping("/{id}/hide")
    @RequireAdmin
    public Result<Boolean> hideMap(
            @Parameter(description = "图谱ID") @PathVariable Long id) {
        
        boolean success = knowledgeMapService.hideMap(id);
        return Result.success(success);
    }
    
    @Operation(summary = "删除知识图谱")
    @DeleteMapping("/{id}")
    @RequireAdmin
    public Result<Boolean> deleteMap(
            @Parameter(description = "图谱ID") @PathVariable Long id) {
        
        boolean success = knowledgeMapService.deleteMap(id);
        return Result.success(success);
    }
    
    @Operation(summary = "批量删除知识图谱")
    @DeleteMapping("/batch")
    @RequireAdmin
    public Result<Boolean> deleteBatch(@RequestBody List<Long> ids) {
        boolean success = knowledgeMapService.deleteBatch(ids);
        return Result.success(success);
    }
} 