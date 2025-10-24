package com.xiaou.codepen.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 作品点赞实体
 * 
 * @author xiaou
 */
@Data
public class CodePenLike {
    
    /**
     * 点赞ID
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
     * 点赞时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}

