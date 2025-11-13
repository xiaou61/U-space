package com.xiaou.codepen.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 代码作品实体
 * 
 * @author xiaou
 */
@Data
public class CodePen {
    
    /**
     * 作品ID
     */
    private Long id;
    
    /**
     * 作者用户ID
     */
    private Long userId;
    
    /**
     * 作品标题
     */
    private String title;
    
    /**
     * 作品描述
     */
    private String description;
    
    /**
     * HTML代码
     */
    private String htmlCode;
    
    /**
     * CSS代码
     */
    private String cssCode;
    
    /**
     * JavaScript代码
     */
    private String jsCode;
    
    /**
     * 预览图URL
     */
    private String previewImage;
    
    /**
     * 外部CSS库链接，JSON数组格式
     */
    private String externalCss;
    
    /**
     * 外部JS库链接，JSON数组格式
     */
    private String externalJs;
    
    /**
     * 预处理器配置，JSON格式
     */
    private String preprocessorConfig;
    
    /**
     * 标签，JSON数组格式
     */
    private String tags;
    
    /**
     * 分类（动画、组件、游戏、工具等）
     */
    private String category;
    
    /**
     * 可见性：1-公开 0-私密
     */
    private Integer isPublic;
    
    /**
     * 是否免费：1-免费 0-付费
     */
    private Integer isFree;
    
    /**
     * Fork价格（积分），0表示免费，1-1000表示付费
     */
    private Integer forkPrice;
    
    /**
     * 是否系统模板：1-是 0-否
     */
    private Integer isTemplate;
    
    /**
     * 状态：0-草稿 1-已发布 2-已下架 3-已删除
     */
    private Integer status;
    
    /**
     * 是否推荐：1-推荐 0-普通
     */
    private Integer isRecommend;
    
    /**
     * 推荐过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date recommendExpireTime;
    
    /**
     * Fork来源作品ID
     */
    private Long forkedFrom;
    
    /**
     * Fork次数
     */
    private Integer forkCount;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 评论数
     */
    private Integer commentCount;
    
    /**
     * 收藏数
     */
    private Integer collectCount;
    
    /**
     * 浏览数
     */
    private Integer viewCount;
    
    /**
     * 创建奖励积分
     */
    private Integer pointsReward;
    
    /**
     * 累计收益积分（通过付费Fork获得）
     */
    private Integer totalIncome;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    
    /**
     * 发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date publishTime;
}

