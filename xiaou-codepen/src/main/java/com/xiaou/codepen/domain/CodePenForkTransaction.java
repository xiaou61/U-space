package com.xiaou.codepen.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * Fork交易记录实体
 * 
 * @author xiaou
 */
@Data
public class CodePenForkTransaction {
    
    /**
     * 交易ID
     */
    private Long id;
    
    /**
     * 原作品ID
     */
    private Long originalPenId;
    
    /**
     * Fork后的作品ID
     */
    private Long forkedPenId;
    
    /**
     * 原作者ID
     */
    private Long originalAuthorId;
    
    /**
     * Fork用户ID
     */
    private Long forkUserId;
    
    /**
     * Fork价格（积分），0表示免费
     */
    private Integer forkPrice;
    
    /**
     * 交易类型：0-免费Fork 1-付费Fork
     */
    private Integer transactionType;
    
    /**
     * 交易时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}

