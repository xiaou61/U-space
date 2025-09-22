package com.xiaou.moyu.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 热榜条目实体
 *
 * @author xiaou
 */
@Data
public class HotTopicItem {
    
    /**
     * 条目ID
     */
    private Long id;
    
    /**
     * 标题
     */
    private String title;
    
    /**
     * 封面图片
     */
    private String cover;
    
    /**
     * 描述
     */
    private String desc;
    
    /**
     * 作者
     */
    private String author;
    
    /**
     * 时间戳
     */
    private Long timestamp;
    
    /**
     * 热度值
     */
    private Long hot;
    
    /**
     * 链接地址
     */
    private String url;
    
    /**
     * 移动端链接
     */
    private String mobileUrl;
}
