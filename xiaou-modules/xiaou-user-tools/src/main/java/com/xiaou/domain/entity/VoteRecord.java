package com.xiaou.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName u_vote_record
 */
@TableName(value ="u_vote_record")
@Data
public class VoteRecord {
    /**
     * 记录ID
     */
    @TableId
    private Long id;

    /**
     * 投票ID
     */
    private Long voteId;

    /**
     * 投票用户ID
     */
    private Long userId;

    /**
     * 选中的选项ID
     */
    private Long optionId;

    /**
     * 
     */
    private Date voteTime;
}