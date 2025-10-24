package com.xiaou.codepen.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 作品收藏实体
 * 
 * @author xiaou
 */
@Data
public class CodePenCollect {
    
    /**
     * 收藏ID
     */
    private Long id;
    
    /**
     * 作品ID
     */
    private Long penId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 收藏夹ID
     */
    private Long folderId;
    
    /**
     * 收藏时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}

