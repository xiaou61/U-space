package com.xiaou.moment.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 动态收藏实体
 */
@Data
public class MomentFavorite {
    
    /**
     * 收藏ID
     */
    private Long id;
    
    /**
     * 动态ID
     */
    private Long momentId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 收藏时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}

