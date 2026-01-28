package com.xiaou.codepen.controller.user;

import com.xiaou.codepen.domain.CodePen;
import com.xiaou.codepen.domain.CodePenComment;
import com.xiaou.codepen.domain.CodePenFolder;
import com.xiaou.codepen.domain.CodePenTag;
import com.xiaou.codepen.dto.*;
import com.xiaou.codepen.service.CodePenCommentService;
import com.xiaou.codepen.service.CodePenFolderService;
import com.xiaou.codepen.service.CodePenService;
import com.xiaou.codepen.service.CodePenTagService;
import com.xiaou.common.annotation.Log;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.core.domain.Result;
import com.xiaou.common.satoken.StpUserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 代码共享器用户端控制器
 * 
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/user/code-pen")
@RequiredArgsConstructor
public class CodePenUserController {
    
    private final CodePenService codePenService;
    private final CodePenCommentService commentService;
    private final CodePenTagService tagService;
    private final CodePenFolderService folderService;
    
    // ========== 作品管理相关 ==========
    
    /**
     * 创建/保存作品（首次发布奖励10积分）
     */
    @Log(module = "代码共享器", type = Log.OperationType.INSERT, description = "创建作品")
    @PostMapping("/create")
    public Result<CodePenCreateResponse> create(@RequestBody CodePenCreateRequest request) {
        try {
            StpUserUtil.checkLogin();
            Long userId = StpUserUtil.getLoginIdAsLong();
            CodePenCreateResponse response = codePenService.createOrSave(request, userId);
            return Result.success(response);
        } catch (Exception e) {
            log.error("创建作品失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 保存作品
     */
    @Log(module = "代码共享器", type = Log.OperationType.UPDATE, description = "保存作品")
    @PostMapping("/save")
    public Result<CodePenCreateResponse> save(@RequestBody CodePenCreateRequest request) {
        try {
            StpUserUtil.checkLogin();
            Long userId = StpUserUtil.getLoginIdAsLong();
            CodePenCreateResponse response = codePenService.createOrSave(request, userId);
            return Result.success(response);
        } catch (Exception e) {
            log.error("保存作品失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新作品
     */
    @Log(module = "代码共享器", type = Log.OperationType.UPDATE, description = "更新作品")
    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody CodePenCreateRequest request) {
        try {
            StpUserUtil.checkLogin();
            Long userId = StpUserUtil.getLoginIdAsLong();
            boolean result = codePenService.update(request, userId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("更新作品失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除作品
     */
    @Log(module = "代码共享器", type = Log.OperationType.DELETE, description = "删除作品")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        try {
            StpUserUtil.checkLogin();
            Long userId = StpUserUtil.getLoginIdAsLong();
            boolean result = codePenService.delete(id, userId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("删除作品失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * Fork作品（免费或付费）
     */
    @Log(module = "代码共享器", type = Log.OperationType.INSERT, description = "Fork作品")
    @PostMapping("/fork")
    public Result<ForkResponse> fork(@RequestBody ForkRequest request) {
        try {
            StpUserUtil.checkLogin();
            Long userId = StpUserUtil.getLoginIdAsLong();
            ForkResponse response = codePenService.forkPen(request, userId);
            return Result.success(response);
        } catch (Exception e) {
            log.error("Fork作品失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取作品详情
     */
    @GetMapping("/{id}")
    public Result<CodePenDetailResponse> getDetail(@PathVariable Long id) {
        try {
            Long currentUserId = StpUserUtil.isLogin() ? StpUserUtil.getLoginIdAsLong() : null;
            CodePenDetailResponse response = codePenService.getDetail(id, currentUserId);
            return Result.success(response);
        } catch (Exception e) {
            log.error("获取作品详情失败，id: {}", id, e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 我的作品列表
     */
    @PostMapping("/my-list")
    public Result<List<CodePenDetailResponse>> getMyList(@RequestBody(required = false) CodePenListRequest request) {
        try {
            StpUserUtil.checkLogin();
            Long userId = StpUserUtil.getLoginIdAsLong();
            Integer status = request != null ? request.getStatus() : null;
            List<CodePenDetailResponse> list = codePenService.getMyList(userId, status);
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取我的作品列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 我的草稿列表
     */
    @PostMapping("/draft-list")
    public Result<List<CodePenDetailResponse>> getDraftList() {
        try {
            StpUserUtil.checkLogin();
            Long userId = StpUserUtil.getLoginIdAsLong();
            List<CodePenDetailResponse> list = codePenService.getMyList(userId, 0);
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取草稿列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 检查Fork价格和用户积分
     */
    @PostMapping("/check-fork-price")
    public Result<CheckForkPriceResponse> checkForkPrice(@RequestBody CheckForkPriceRequest request) {
        try {
            StpUserUtil.checkLogin();
            Long userId = StpUserUtil.getLoginIdAsLong();
            CheckForkPriceResponse response = codePenService.checkForkPrice(request.getPenId(), userId);
            return Result.success(response);
        } catch (Exception e) {
            log.error("检查Fork价格失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 查看收益统计
     */
    @PostMapping("/income-stats")
    public Result<IncomeStatsResponse> getIncomeStats() {
        try {
            StpUserUtil.checkLogin();
            Long userId = StpUserUtil.getLoginIdAsLong();
            IncomeStatsResponse response = codePenService.getIncomeStats(userId);
            return Result.success(response);
        } catch (Exception e) {
            log.error("获取收益统计失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    // ========== 作品广场相关 ==========
    
    /**
     * 获取作品列表（代码广场）
     */
    @PostMapping("/list")
    public Result<PageResult<CodePenDetailResponse>> getList(@RequestBody CodePenListRequest request) {
        try {
            Long currentUserId = StpUserUtil.isLogin() ? StpUserUtil.getLoginIdAsLong() : null;
            PageResult<CodePenDetailResponse> pageResult = codePenService.getList(request, currentUserId);
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("获取作品列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取推荐作品列表
     */
    @PostMapping("/recommend-list")
    public Result<List<CodePenDetailResponse>> getRecommendList() {
        try {
            List<CodePenDetailResponse> list = codePenService.getRecommendList(10);
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取推荐作品失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取热门作品
     */
    @GetMapping("/hot")
    public Result<List<CodePenDetailResponse>> getHotList() {
        try {
            List<CodePenDetailResponse> list = codePenService.getHotList(10);
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取热门作品失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 搜索作品
     */
    @PostMapping("/search")
    public Result<PageResult<CodePenDetailResponse>> search(@RequestBody CodePenListRequest request) {
        try {
            Long currentUserId = StpUserUtil.isLogin() ? StpUserUtil.getLoginIdAsLong() : null;
            PageResult<CodePenDetailResponse> pageResult = codePenService.getList(request, currentUserId);
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("搜索作品失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 按标签获取作品
     */
    @PostMapping("/by-tag")
    public Result<PageResult<CodePenDetailResponse>> getByTag(@RequestBody CodePenListRequest request) {
        try {
            Long currentUserId = StpUserUtil.isLogin() ? StpUserUtil.getLoginIdAsLong() : null;
            PageResult<CodePenDetailResponse> pageResult = codePenService.getList(request, currentUserId);
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("按标签获取作品失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 按分类获取作品
     */
    @PostMapping("/by-category")
    public Result<PageResult<CodePenDetailResponse>> getByCategory(@RequestBody CodePenListRequest request) {
        try {
            Long currentUserId = StpUserUtil.isLogin() ? StpUserUtil.getLoginIdAsLong() : null;
            PageResult<CodePenDetailResponse> pageResult = codePenService.getList(request, currentUserId);
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("按分类获取作品失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取指定用户的作品
     */
    @PostMapping("/by-user")
    public Result<PageResult<CodePenDetailResponse>> getByUser(@RequestBody CodePenListRequest request) {
        try {
            Long currentUserId = StpUserUtil.isLogin() ? StpUserUtil.getLoginIdAsLong() : null;
            PageResult<CodePenDetailResponse> pageResult = codePenService.getList(request, currentUserId);
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("获取指定用户作品失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    // ========== 互动功能相关 ==========
    
    /**
     * 点赞作品
     */
    @Log(module = "代码共享器", type = Log.OperationType.INSERT, description = "点赞作品")
    @PostMapping("/like")
    public Result<Boolean> like(@RequestBody CheckForkPriceRequest request) {
        try {
            StpUserUtil.checkLogin();
            Long userId = StpUserUtil.getLoginIdAsLong();
            boolean result = codePenService.like(request.getPenId(), userId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("点赞作品失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 取消点赞
     */
    @Log(module = "代码共享器", type = Log.OperationType.DELETE, description = "取消点赞")
    @PostMapping("/unlike")
    public Result<Boolean> unlike(@RequestBody CheckForkPriceRequest request) {
        try {
            StpUserUtil.checkLogin();
            Long userId = StpUserUtil.getLoginIdAsLong();
            boolean result = codePenService.unlike(request.getPenId(), userId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("取消点赞失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 收藏作品
     */
    @Log(module = "代码共享器", type = Log.OperationType.INSERT, description = "收藏作品")
    @PostMapping("/collect")
    public Result<Boolean> collect(@RequestBody CheckForkPriceRequest request) {
        try {
            StpUserUtil.checkLogin();
            Long userId = StpUserUtil.getLoginIdAsLong();
            boolean result = codePenService.collect(request.getPenId(), userId, null);
            return Result.success(result);
        } catch (Exception e) {
            log.error("收藏作品失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 取消收藏
     */
    @Log(module = "代码共享器", type = Log.OperationType.DELETE, description = "取消收藏")
    @PostMapping("/uncollect")
    public Result<Boolean> uncollect(@RequestBody CheckForkPriceRequest request) {
        try {
            StpUserUtil.checkLogin();
            Long userId = StpUserUtil.getLoginIdAsLong();
            boolean result = codePenService.uncollect(request.getPenId(), userId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("取消收藏失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 增加浏览数
     */
    @PostMapping("/view")
    public Result<Boolean> incrementView(@RequestBody CheckForkPriceRequest request) {
        try {
            boolean result = codePenService.incrementView(request.getPenId());
            return Result.success(result);
        } catch (Exception e) {
            log.error("增加浏览数失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 发表评论
     */
    @Log(module = "代码共享器", type = Log.OperationType.INSERT, description = "发表评论")
    @PostMapping("/comment")
    public Result<Long> createComment(@RequestBody CommentCreateRequest request) {
        try {
            StpUserUtil.checkLogin();
            Long userId = StpUserUtil.getLoginIdAsLong();
            Long commentId = commentService.createComment(request, userId);
            return Result.success(commentId);
        } catch (Exception e) {
            log.error("发表评论失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除评论
     */
    @Log(module = "代码共享器", type = Log.OperationType.DELETE, description = "删除评论")
    @DeleteMapping("/comment/{id}")
    public Result<Boolean> deleteComment(@PathVariable Long id) {
        try {
            StpUserUtil.checkLogin();
            Long userId = StpUserUtil.getLoginIdAsLong();
            boolean result = commentService.deleteComment(id, userId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("删除评论失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取评论列表
     */
    @PostMapping("/comment-list")
    public Result<List<CodePenComment>> getCommentList(@RequestBody CheckForkPriceRequest request) {
        try {
            List<CodePenComment> list = commentService.getCommentList(request.getPenId());
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取评论列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    // ========== 收藏夹管理相关 ==========
    
    /**
     * 创建收藏夹
     */
    @Log(module = "代码共享器", type = Log.OperationType.INSERT, description = "创建收藏夹")
    @PostMapping("/folder/create")
    public Result<Long> createFolder(@RequestBody CodePenFolder folder) {
        try {
            StpUserUtil.checkLogin();
            Long userId = StpUserUtil.getLoginIdAsLong();
            Long folderId = folderService.createFolder(folder.getFolderName(), folder.getFolderDescription(), userId);
            return Result.success(folderId);
        } catch (Exception e) {
            log.error("创建收藏夹失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新收藏夹
     */
    @Log(module = "代码共享器", type = Log.OperationType.UPDATE, description = "更新收藏夹")
    @PostMapping("/folder/update")
    public Result<Boolean> updateFolder(@RequestBody CodePenFolder folder) {
        try {
            StpUserUtil.checkLogin();
            Long userId = StpUserUtil.getLoginIdAsLong();
            boolean result = folderService.updateFolder(folder.getId(), folder.getFolderName(), folder.getFolderDescription(), userId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("更新收藏夹失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除收藏夹
     */
    @Log(module = "代码共享器", type = Log.OperationType.DELETE, description = "删除收藏夹")
    @DeleteMapping("/folder/{id}")
    public Result<Boolean> deleteFolder(@PathVariable Long id) {
        try {
            StpUserUtil.checkLogin();
            Long userId = StpUserUtil.getLoginIdAsLong();
            boolean result = folderService.deleteFolder(id, userId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("删除收藏夹失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 我的收藏夹列表
     */
    @PostMapping("/folder/list")
    public Result<List<CodePenFolder>> getFolderList() {
        try {
            StpUserUtil.checkLogin();
            Long userId = StpUserUtil.getLoginIdAsLong();
            List<CodePenFolder> list = folderService.getFolderList(userId);
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取收藏夹列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 收藏夹内容列表
     */
    @PostMapping("/folder/items")
    public Result<List<CodePenDetailResponse>> getFolderItems(@RequestBody CodePenFolder folder) {
        try {
            StpUserUtil.checkLogin();
            Long userId = StpUserUtil.getLoginIdAsLong();
            
            // 查询收藏夹中的作品ID列表
            List<Long> penIds = folderService.getFolderItems(folder.getId(), userId);
            
            // 查询作品详情
            List<CodePenDetailResponse> responses = new ArrayList<>();
            for (Long penId : penIds) {
                try {
                    CodePenDetailResponse detail = codePenService.getDetail(penId, userId);
                    responses.add(detail);
                } catch (Exception e) {
                    log.warn("获取作品{}详情失败", penId, e);
                }
            }
            
            return Result.success(responses);
        } catch (Exception e) {
            log.error("获取收藏夹内容失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    // ========== 模板管理相关 ==========
    
    /**
     * 获取系统模板列表
     */
    @GetMapping("/templates")
    public Result<List<CodePen>> getTemplates() {
        try {
            List<CodePen> list = codePenService.getTemplateList();
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取模板列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取模板详情
     */
    @GetMapping("/template/{id}")
    public Result<CodePenDetailResponse> getTemplateDetail(@PathVariable Long id) {
        try {
            Long currentUserId = StpUserUtil.isLogin() ? StpUserUtil.getLoginIdAsLong() : null;
            CodePenDetailResponse response = codePenService.getDetail(id, currentUserId);
            return Result.success(response);
        } catch (Exception e) {
            log.error("获取模板详情失败，id: {}", id, e);
            return Result.error(e.getMessage());
        }
    }
    
    // ========== 标签管理相关 ==========
    
    /**
     * 获取所有标签
     */
    @GetMapping("/tags")
    public Result<List<CodePenTag>> getTags() {
        try {
            List<CodePenTag> list = tagService.getAllTags();
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取标签列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取热门标签
     */
    @GetMapping("/tags/hot")
    public Result<List<CodePenTag>> getHotTags() {
        try {
            List<CodePenTag> list = tagService.getHotTags(20);
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取热门标签失败", e);
            return Result.error(e.getMessage());
        }
    }
}

