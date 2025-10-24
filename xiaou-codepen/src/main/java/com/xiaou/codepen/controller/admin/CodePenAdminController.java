package com.xiaou.codepen.controller.admin;

import com.xiaou.codepen.domain.CodePen;
import com.xiaou.codepen.domain.CodePenTag;
import com.xiaou.codepen.dto.*;
import com.xiaou.codepen.mapper.CodePenMapper;
import com.xiaou.codepen.service.CodePenCommentService;
import com.xiaou.codepen.service.CodePenService;
import com.xiaou.codepen.service.CodePenTagService;
import com.xiaou.common.annotation.Log;
import com.xiaou.common.annotation.RequireAdmin;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.core.domain.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 代码共享器管理端控制器
 * 
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/admin/code-pen")
@RequiredArgsConstructor
public class CodePenAdminController {
    
    private final CodePenService codePenService;
    private final CodePenMapper codePenMapper;
    private final CodePenTagService tagService;
    private final CodePenCommentService commentService;
    
    // ========== 作品管理相关 ==========
    
    /**
     * 获取作品列表（管理端）
     */
    @RequireAdmin
    @PostMapping("/list")
    public Result<PageResult<CodePenDetailResponse>> getList(@RequestBody CodePenListRequest request) {
        try {
            PageResult<CodePenDetailResponse> pageResult = codePenService.getList(request, null);
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("获取作品列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取作品详情
     */
    @RequireAdmin
    @GetMapping("/{id}")
    public Result<CodePenDetailResponse> getDetail(@PathVariable Long id) {
        try {
            CodePenDetailResponse response = codePenService.getDetail(id, null);
            // 管理员可以查看所有源码
            response.setCanViewCode(true);
            return Result.success(response);
        } catch (Exception e) {
            log.error("获取作品详情失败，id: {}", id, e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新作品状态
     */
    @RequireAdmin
    @Log(module = "代码共享器", type = Log.OperationType.UPDATE, description = "更新作品状态")
    @PostMapping("/update-status")
    public Result<Boolean> updateStatus(@RequestBody UpdateStatusRequest request) {
        try {
            int result = codePenMapper.updateStatus(request.getId(), request.getStatus());
            return Result.success(result > 0);
        } catch (Exception e) {
            log.error("更新作品状态失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 设置推荐
     */
    @RequireAdmin
    @Log(module = "代码共享器", type = Log.OperationType.UPDATE, description = "设置推荐")
    @PostMapping("/recommend")
    public Result<Boolean> setRecommend(@RequestBody RecommendRequest request) {
        try {
            int result = codePenMapper.setRecommend(request.getId(), request.getExpireTime());
            return Result.success(result > 0);
        } catch (Exception e) {
            log.error("设置推荐失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 取消推荐
     */
    @RequireAdmin
    @Log(module = "代码共享器", type = Log.OperationType.UPDATE, description = "取消推荐")
    @PostMapping("/cancel-recommend")
    public Result<Boolean> cancelRecommend(@RequestBody CheckForkPriceRequest request) {
        try {
            int result = codePenMapper.cancelRecommend(request.getPenId());
            return Result.success(result > 0);
        } catch (Exception e) {
            log.error("取消推荐失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除作品（管理端）
     */
    @RequireAdmin
    @Log(module = "代码共享器", type = Log.OperationType.DELETE, description = "删除作品")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        try {
            int result = codePenMapper.deleteById(id);
            return Result.success(result > 0);
        } catch (Exception e) {
            log.error("删除作品失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    // ========== 模板管理相关 ==========
    
    /**
     * 创建系统模板
     */
    @RequireAdmin
    @Log(module = "代码共享器", type = Log.OperationType.INSERT, description = "创建系统模板")
    @PostMapping("/template/create")
    public Result<CodePenCreateResponse> createTemplate(@RequestBody CodePenCreateRequest request) {
        try {
            // 设置为系统模板
            CodePen template = new CodePen();
            template.setUserId(1L); // 系统用户
            template.setTitle(request.getTitle());
            template.setDescription(request.getDescription());
            template.setHtmlCode(request.getHtmlCode());
            template.setCssCode(request.getCssCode());
            template.setJsCode(request.getJsCode());
            template.setIsTemplate(1);
            template.setStatus(1);
            template.setIsPublic(1);
            template.setIsFree(1);
            template.setForkPrice(0);
            
            codePenMapper.insert(template);
            
            CodePenCreateResponse response = CodePenCreateResponse.builder()
                    .penId(template.getId())
                    .createTime(template.getCreateTime())
                    .build();
            
            return Result.success(response);
        } catch (Exception e) {
            log.error("创建系统模板失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新模板
     */
    @RequireAdmin
    @Log(module = "代码共享器", type = Log.OperationType.UPDATE, description = "更新模板")
    @PostMapping("/template/update")
    public Result<Boolean> updateTemplate(@RequestBody CodePenCreateRequest request) {
        try {
            if (request.getId() == null) {
                return Result.error("模板ID不能为空");
            }
            CodePen template = codePenMapper.selectById(request.getId());
            if (template == null || template.getIsTemplate() != 1) {
                return Result.error("模板不存在");
            }
            
            CodePen updateTemplate = new CodePen();
            updateTemplate.setId(request.getId());
            updateTemplate.setTitle(request.getTitle());
            updateTemplate.setDescription(request.getDescription());
            updateTemplate.setHtmlCode(request.getHtmlCode());
            updateTemplate.setCssCode(request.getCssCode());
            updateTemplate.setJsCode(request.getJsCode());
            
            int result = codePenMapper.updateById(updateTemplate);
            return Result.success(result > 0);
        } catch (Exception e) {
            log.error("更新模板失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 模板列表
     */
    @RequireAdmin
    @PostMapping("/template/list")
    public Result<List<CodePen>> getTemplateList() {
        try {
            List<CodePen> list = codePenService.getTemplateList();
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取模板列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除模板
     */
    @RequireAdmin
    @Log(module = "代码共享器", type = Log.OperationType.DELETE, description = "删除模板")
    @DeleteMapping("/template/{id}")
    public Result<Boolean> deleteTemplate(@PathVariable Long id) {
        try {
            int result = codePenMapper.deleteById(id);
            return Result.success(result > 0);
        } catch (Exception e) {
            log.error("删除模板失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    // ========== 标签管理相关 ==========
    
    /**
     * 创建标签
     */
    @RequireAdmin
    @Log(module = "代码共享器", type = Log.OperationType.INSERT, description = "创建标签")
    @PostMapping("/tag/create")
    public Result<Long> createTag(@RequestBody CodePenTag tag) {
        try {
            Long tagId = tagService.createTag(tag.getTagName(), tag.getTagDescription());
            return Result.success(tagId);
        } catch (Exception e) {
            log.error("创建标签失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新标签
     */
    @RequireAdmin
    @Log(module = "代码共享器", type = Log.OperationType.UPDATE, description = "更新标签")
    @PostMapping("/tag/update")
    public Result<Boolean> updateTag(@RequestBody CodePenTag tag) {
        try {
            boolean result = tagService.updateTag(tag.getId(), tag.getTagName(), tag.getTagDescription());
            return Result.success(result);
        } catch (Exception e) {
            log.error("更新标签失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除标签
     */
    @RequireAdmin
    @Log(module = "代码共享器", type = Log.OperationType.DELETE, description = "删除标签")
    @DeleteMapping("/tag/{id}")
    public Result<Boolean> deleteTag(@PathVariable Long id) {
        try {
            boolean result = tagService.deleteTag(id);
            return Result.success(result);
        } catch (Exception e) {
            log.error("删除标签失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 合并标签
     */
    @RequireAdmin
    @Log(module = "代码共享器", type = Log.OperationType.UPDATE, description = "合并标签")
    @PostMapping("/tag/merge")
    public Result<Boolean> mergeTags(@RequestBody MergeTagRequest request) {
        try {
            boolean result = tagService.mergeTags(request.getSourceId(), request.getTargetId());
            return Result.success(result);
        } catch (Exception e) {
            log.error("合并标签失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 标签列表
     */
    @RequireAdmin
    @PostMapping("/tag/list")
    public Result<List<CodePenTag>> getTagList() {
        try {
            List<CodePenTag> list = tagService.getAllTags();
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取标签列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    // ========== 评论管理相关 ==========
    
    /**
     * 评论列表
     */
    @RequireAdmin
    @PostMapping("/comment/list")
    public Result<List<com.xiaou.codepen.domain.CodePenComment>> getCommentList(@RequestBody CheckForkPriceRequest request) {
        try {
            List<com.xiaou.codepen.domain.CodePenComment> list = commentService.getCommentList(request.getPenId());
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取评论列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 隐藏评论
     */
    @RequireAdmin
    @Log(module = "代码共享器", type = Log.OperationType.UPDATE, description = "隐藏评论")
    @PostMapping("/comment/hide")
    public Result<Boolean> hideComment(@RequestBody UpdateStatusRequest request) {
        try {
            boolean result = commentService.hideComment(request.getId());
            return Result.success(result);
        } catch (Exception e) {
            log.error("隐藏评论失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除评论
     */
    @RequireAdmin
    @Log(module = "代码共享器", type = Log.OperationType.DELETE, description = "删除评论")
    @DeleteMapping("/comment/{id}")
    public Result<Boolean> deleteComment(@PathVariable Long id) {
        try {
            boolean result = commentService.adminDeleteComment(id);
            return Result.success(result);
        } catch (Exception e) {
            log.error("删除评论失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    // ========== 基础数据相关 ==========
    
    /**
     * 获取统计数据
     */
    @RequireAdmin
    @GetMapping("/statistics")
    public Result<CodePenStatisticsResponse> getStatistics() {
        try {
            CodePenStatisticsResponse response = codePenService.getStatistics();
            return Result.success(response);
        } catch (Exception e) {
            log.error("获取统计数据失败", e);
            return Result.error(e.getMessage());
        }
    }
}

