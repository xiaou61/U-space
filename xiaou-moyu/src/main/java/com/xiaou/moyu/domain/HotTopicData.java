package com.xiaou.moyu.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.xiaou.moyu.utils.FlexibleDateDeserializer;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 热榜数据实体
 *
 * @author xiaou
 */
@Data
public class HotTopicData {
    
    /**
     * 平台名称
     */
    private String name;
    
    /**
     * 平台标题
     */
    private String title;
    
    /**
     * 类型
     */
    private String type;
    
    /**
     * 链接
     */
    private String link;
    
    /**
     * 总数
     */
    private Integer total;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonDeserialize(using = FlexibleDateDeserializer.class)
    private Date updatedAt;
    
    /**
     * 热榜条目列表
     */
    private List<HotTopicItem> data;
    
    /**
     * 是否来自缓存
     */
    private Boolean fromCache;
}
