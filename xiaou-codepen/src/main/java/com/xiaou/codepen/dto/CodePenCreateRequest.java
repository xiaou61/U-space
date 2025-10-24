package com.xiaou.codepen.dto;

import lombok.Data;

import java.util.List;

/**
 * 作品创建/保存请求
 * 
 * @author xiaou
 */
@Data
public class CodePenCreateRequest {
    
    /**
     * 作品ID（编辑时传入）
     */
    private Long id;
    
    /**
     * 作品标题（必填，1-100字符）
     */
    private String title;
    
    /**
     * 作品描述（选填，最多500字符）
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
     * 外部CSS库链接列表
     */
    private List<String> externalCss;
    
    /**
     * 外部JS库链接列表
     */
    private List<String> externalJs;
    
    /**
     * 标签列表（最多5个）
     */
    private List<String> tags;
    
    /**
     * 分类
     */
    private String category;
    
    /**
     * 可见性：1-公开 0-私密，默认1
     */
    private Integer isPublic;
    
    /**
     * 是否免费：1-免费 0-付费，默认1
     */
    private Integer isFree;
    
    /**
     * Fork价格（0或1-1000积分）
     */
    private Integer forkPrice;
    
    /**
     * 状态：0-草稿 1-发布
     */
    private Integer status;
}

