package com.xiaou.codepen.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 收藏夹实体
 * 
 * @author xiaou
 */
@Data
public class CodePenFolder {
    
    /**
     * 收藏夹ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 收藏夹名称
     */
    private String folderName;
    
    /**
     * 收藏夹描述
     */
    private String folderDescription;
    
    /**
     * 收藏数量
     */
    private Integer collectCount;
    
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
}

