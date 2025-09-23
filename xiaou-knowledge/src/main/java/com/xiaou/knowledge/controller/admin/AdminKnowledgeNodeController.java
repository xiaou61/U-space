package com.xiaou.knowledge.controller.admin;

import com.xiaou.common.annotation.RequireAdmin;

import com.xiaou.common.core.domain.Result;
import com.xiaou.knowledge.domain.KnowledgeNode;
import com.xiaou.knowledge.dto.request.CreateKnowledgeNodeRequest;
import com.xiaou.knowledge.dto.request.SortNodesRequest;
import com.xiaou.knowledge.dto.request.UpdateKnowledgeNodeRequest;
import com.xiaou.knowledge.dto.response.KnowledgeNodeTreeResponse;
import com.xiaou.knowledge.service.KnowledgeNodeService;
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
 * 管理员端知识节点控制器
 * 
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/admin/knowledge")
@RequiredArgsConstructor
@Tag(name = "管理员端知识节点管理", description = "管理员端知识节点相关接口")
@Validated
public class AdminKnowledgeNodeController {
    
    private final KnowledgeNodeService knowledgeNodeService;
    
    @Operation(summary = "获取图谱节点树")
    @GetMapping("/maps/{mapId}/nodes")
    @RequireAdmin
    public Result<List<KnowledgeNodeTreeResponse>> getNodeTree(
            @Parameter(description = "图谱ID") @PathVariable Long mapId) {
        
        List<KnowledgeNodeTreeResponse> nodeTree = knowledgeNodeService.getTreeByMapId(mapId);
        return Result.success(nodeTree);
    }
    
    @Operation(summary = "根据ID获取知识节点详情")
    @GetMapping("/nodes/{id}")
    @RequireAdmin
    public Result<KnowledgeNode> getNodeById(
            @Parameter(description = "节点ID") @PathVariable Long id) {
        
        KnowledgeNode knowledgeNode = knowledgeNodeService.getById(id);
        return Result.success(knowledgeNode);
    }
    
    @Operation(summary = "创建知识节点")
    @PostMapping("/maps/{mapId}/nodes")
    @RequireAdmin
    public Result<Long> createNode(
            @Parameter(description = "图谱ID") @PathVariable Long mapId,
            @Valid @RequestBody CreateKnowledgeNodeRequest request) {
        
        Long nodeId = knowledgeNodeService.createNode(mapId, request);
        return Result.success(nodeId);
    }
    
    @Operation(summary = "更新知识节点")
    @PutMapping("/nodes/{id}")
    @RequireAdmin
    public Result<Boolean> updateNode(
            @Parameter(description = "节点ID") @PathVariable Long id,
            @Valid @RequestBody UpdateKnowledgeNodeRequest request) {
        
        boolean success = knowledgeNodeService.updateNode(id, request);
        return Result.success(success);
    }
    
    @Operation(summary = "批量更新节点排序")
    @PutMapping("/maps/{mapId}/nodes/sort")
    @RequireAdmin
    public Result<Boolean> sortNodes(
            @Parameter(description = "图谱ID") @PathVariable Long mapId,
            @Valid @RequestBody SortNodesRequest request) {
        
        boolean success = knowledgeNodeService.sortNodes(mapId, request);
        return Result.success(success);
    }
    
    @Operation(summary = "删除知识节点")
    @DeleteMapping("/nodes/{id}")
    @RequireAdmin
    public Result<Boolean> deleteNode(
            @Parameter(description = "节点ID") @PathVariable Long id) {
        
        boolean success = knowledgeNodeService.deleteNode(id);
        return Result.success(success);
    }
    
    @Operation(summary = "搜索节点")
    @GetMapping("/maps/{mapId}/nodes/search")
    @RequireAdmin
    public Result<List<KnowledgeNodeTreeResponse>> searchNodes(
            @Parameter(description = "图谱ID") @PathVariable Long mapId,
            @Parameter(description = "搜索关键词") @RequestParam String keyword) {
        
        List<KnowledgeNodeTreeResponse> nodes = knowledgeNodeService.searchNodes(mapId, keyword);
        return Result.success(nodes);
    }
} 