package com.xiaou.moyu.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 热榜平台枚举
 *
 * @author xiaou
 */
@Getter
@AllArgsConstructor
public enum HotTopicEnum {
    
    // 社交媒体
    DOUYIN("douyin", "抖音 - 热榜", "社交媒体"),
    KUAISHOU("kuaishou", "快手 - 热榜", "社交媒体"),
    WEIBO("weibo", "微博 - 热搜榜", "社交媒体"),
    
    // 知识社区
    HUPU("hupu", "虎扑 - 步行街热帖", "知识社区"),
    LINUXDO("linuxdo", "Linux.do - 热门文章", "知识社区"),
    NEWSMTH("newsmth", "水木社区 - 热门话题", "知识社区"),
    TIEBA("tieba", "百度贴吧 - 热议榜", "知识社区"),
    ZHIHU("zhihu", "知乎 - 热榜", "知识社区"),
    ZHIHU_DAILY("zhihu-daily", "知乎日报 - 推荐榜", "知识社区"),
    
    // 新闻资讯
    IFANR("ifanr", "爱范儿 - 快讯", "新闻资讯"),
    NETEASE_NEWS("netease-news", "网易新闻 - 热点榜", "新闻资讯"),
    TOUTIAO("toutiao", "今日头条 - 热榜", "新闻资讯"),
    
    // 科技数码
    CSDN("csdn", "CSDN - 专业开发者社区热榜", "科技数码"),
    DGTLE("dgtle", "数字尾巴 - 热门文章", "科技数码"),
    GEEKPARK("geekpark", "极客公园 - 热门文章", "科技数码"),
    GUOKR("guokr", "果壳 - 热门文章", "科技数码"),
    HELLOGITHUB("hellogithub", "HelloGitHub - 热门仓库", "科技数码"),
    ITHOME("ithome", "IT之家 - 热榜", "科技数码"),
    JUEJIN("juejin", "稀土掘金 - 文章榜", "科技数码"),
    
    // 娱乐生活
    DOUBAN_MOVIE("douban-movie", "豆瓣电影 - 新片榜", "娱乐生活"),
    JIANSHU("jianshu", "简书 - 热门推荐", "娱乐生活"),
    WEREAD("weread", "微信读书 - 排行榜", "娱乐生活"),
    
    // 实用信息
    EARTHQUAKE("earthquake", "中国地震台 - 地震速报", "实用信息"),
    HISTORY("history", "历史上的今天 - 历史事件", "实用信息"),
    WEATHER_ALARM("weather-alarm", "中央气象台 - 气象预警", "实用信息");
    
    /**
     * 平台代码
     */
    private final String code;
    
    /**
     * 平台名称
     */
    private final String name;
    
    /**
     * 分类
     */
    private final String category;
    
    /**
     * 根据代码获取枚举
     */
    public static HotTopicEnum getByCode(String code) {
        for (HotTopicEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }
    
    /**
     * 获取API URL
     */
    public String getApiUrl(String baseUrl) {
        return baseUrl + "/" + this.code;
    }
}
