package com.xiaou.moyu.service;

import com.xiaou.moyu.domain.HotTopicData;
import com.xiaou.moyu.domain.HotTopicResponse;

import java.util.Map;

/**
 * 热榜服务接口
 *
 * @author xiaou
 */
public interface HotTopicService {
    
    /**
     * 获取热榜分类信息
     * 先从缓存获取，缓存不存在则调用API
     *
     * @return 热榜分类响应
     */
    HotTopicResponse getHotTopicCategories();
    
    /**
     * 获取指定平台热榜数据
     * 先从缓存获取，缓存不存在则调用API
     *
     * @param platform 平台代码
     * @return 热榜数据
     */
    HotTopicData getHotTopicData(String platform);
    
    /**
     * 获取所有平台热榜数据
     * 先从缓存获取，缓存不存在则调用API
     *
     * @return 所有平台热榜数据
     */
    Map<String, HotTopicData> getAllHotTopicData();
    
    /**
     * 刷新热榜数据
     * 定时任务调用，强制刷新所有数据
     */
    void refreshHotTopicData();
}
