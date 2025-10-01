package com.xiaou.interview.controller.pub;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.core.domain.Result;
import com.xiaou.common.satoken.StpUserUtil;
import com.xiaou.interview.domain.InterviewFavorite;
import com.xiaou.interview.service.InterviewFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 面试收藏接口
 *
 * @author xiaou
 */
@Tag(name = "面试收藏", description = "用户收藏相关接口")
@RestController
@RequestMapping("/interview/favorites")
@RequiredArgsConstructor
public class InterviewFavoriteController {

    private final InterviewFavoriteService favoriteService;

    /**
     * 收藏操作请求参数
     */
    @Data
    public static class FavoriteRequest {
        private Integer targetType;
        private Long targetId;
    }

    /**
     * 分页查询请求参数
     */
    @Data
    public static class FavoritePageRequest {
        private Integer targetType;
        private Integer page = 1;
        private Integer size = 10;
    }

    @Operation(summary = "添加收藏")
    @PostMapping("/add")
    public Result<Void> addFavorite(@RequestBody FavoriteRequest request) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        if (userId == null) {
            return Result.error("请先登录");
        }
        
        favoriteService.addFavorite(userId, request.getTargetType(), request.getTargetId());
        return Result.success();
    }

    @Operation(summary = "取消收藏")
    @PostMapping("/remove")
    public Result<Void> removeFavorite(@RequestBody FavoriteRequest request) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        if (userId == null) {
            return Result.error("请先登录");
        }
        
        favoriteService.removeFavorite(userId, request.getTargetType(), request.getTargetId());
        return Result.success();
    }

    @Operation(summary = "检查是否已收藏")
    @PostMapping("/check")
    public Result<Boolean> isFavorited(@RequestBody FavoriteRequest request) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        if (userId == null) {
            return Result.success(false);
        }
        
        boolean favorited = favoriteService.isFavorited(userId, request.getTargetType(), request.getTargetId());
        return Result.success(favorited);
    }

    @Operation(summary = "获取我的收藏列表")
    @PostMapping("/my")
    public Result<List<InterviewFavorite>> getMyFavorites(@RequestBody FavoriteRequest request) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        if (userId == null) {
            return Result.error("请先登录");
        }
        
        List<InterviewFavorite> favorites = favoriteService.getUserFavorites(userId, request.getTargetType());
        return Result.success(favorites);
    }

    @Operation(summary = "分页获取我的收藏列表")
    @PostMapping("/my/page")
    public Result<PageResult<InterviewFavorite>> getMyFavoritePage(@RequestBody FavoritePageRequest request) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        if (userId == null) {
            return Result.error("请先登录");
        }
        
        PageResult<InterviewFavorite> result = favoriteService.getUserFavoritePage(
            userId, 
            request.getTargetType(), 
            request.getPage(), 
            request.getSize()
        );
        return Result.success(result);
    }

    @Operation(summary = "获取收藏统计")
    @PostMapping("/count")
    public Result<Integer> getFavoriteCount(@RequestBody FavoriteRequest request) {
        int count = favoriteService.getFavoriteCount(request.getTargetType(), request.getTargetId());
        return Result.success(count);
    }
} 