package com.xiaou.moyu.controller;

import com.xiaou.common.annotation.Log;
import com.xiaou.common.core.domain.Result;
import com.xiaou.moyu.domain.HotTopicData;
import com.xiaou.moyu.domain.HotTopicResponse;
import com.xiaou.moyu.service.HotTopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 热榜控制器
 *
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/moyu/hot-topic")
@RequiredArgsConstructor
public class HotTopicController {
    
    private final HotTopicService hotTopicService;
    
    /**
     * 获取热榜分类信息
     */
    @Log(module = "摸鱼工具", type = Log.OperationType.SELECT, description = "获取热榜分类信息")
    @GetMapping("/categories")
    public Result<HotTopicResponse> getCategories() {
        HotTopicResponse response = hotTopicService.getHotTopicCategories();
        return Result.success(response);
    }
    
    /**
     * 获取指定平台热榜数据
     */
    @Log(module = "摸鱼工具", type = Log.OperationType.SELECT, description = "获取指定平台热榜数据")
    @GetMapping("/data/{platform}")
    public Result<HotTopicData> getHotTopicData(@PathVariable String platform) {
        HotTopicData data = hotTopicService.getHotTopicData(platform);
        return Result.success(data);
    }
    
    /**
     * 获取所有平台热榜数据
     */
    @Log(module = "摸鱼工具", type = Log.OperationType.SELECT, description = "获取所有平台热榜数据")
    @GetMapping("/data/all")
    public Result<Map<String, HotTopicData>> getAllHotTopicData() {
        Map<String, HotTopicData> data = hotTopicService.getAllHotTopicData();
        return Result.success(data);
    }
    
    /**
     * 手动刷新热榜数据
     */
    @Log(module = "摸鱼工具", type = Log.OperationType.UPDATE, description = "手动刷新热榜数据")
    @PostMapping("/refresh")
    public Result<Void> refreshHotTopicData() {
        hotTopicService.refreshHotTopicData();
        return Result.success();
    }
}
