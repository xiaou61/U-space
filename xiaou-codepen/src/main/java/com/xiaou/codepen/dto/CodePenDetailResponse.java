package com.xiaou.codepen.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 作品详情响应
 * 
 * @author xiaou
 */
@Data
public class CodePenDetailResponse {
    
    /**
     * 作品ID
     */
    private Long id;
    
    /**
     * 作者ID
     */
    private Long userId;
    
    /**
     * 作者昵称
     */
    private String userNickname;
    
    /**
     * 作者头像
     */
    private String userAvatar;
    
    /**
     * 作品标题
     */
    private String title;
    
    /**
     * 作品描述
     */
    private String description;
    
    /**
     * HTML代码（付费作品未购买时为null）
     */
    private String htmlCode;
    
    /**
     * CSS代码（付费作品未购买时为null）
     */
    private String cssCode;
    
    /**
     * JavaScript代码（付费作品未购买时为null）
     */
    private String jsCode;
    
    /**
     * 外部CSS库
     */
    private List<String> externalCss;
    
    /**
     * 外部JS库
     */
    private List<String> externalJs;
    
    /**
     * 预览图
     */
    private String previewImage;
    
    /**
     * 标签
     */
    private List<String> tags;
    
    /**
     * 分类
     */
    private String category;
    
    /**
     * 可见性
     */
    private Integer isPublic;
    
    /**
     * 是否免费
     */
    private Boolean isFree;
    
    /**
     * Fork价格
     */
    private Integer forkPrice;
    
    /**
     * Fork来源
     */
    private Long forkedFrom;
    
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
     * Fork数
     */
    private Integer forkCount;
    
    /**
     * 累计收益积分
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
     * 是否已点赞
     */
    private Boolean isLiked;
    
    /**
     * 是否已收藏
     */
    private Boolean isCollected;
    
    /**
     * 是否可编辑
     */
    private Boolean canEdit;
    
    /**
     * 是否可删除
     */
    private Boolean canDelete;
    
    /**
     * 是否可查看源码
     */
    private Boolean canViewCode;
    
    /**
     * 源码查看提示信息
     */
    private String codeViewMessage;
    
    /**
     * 分享链接
     */
    private String shareUrl;
}

