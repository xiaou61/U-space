package com.xiaou.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName u_vote_option
 */
@TableName(value ="u_vote_option")
@Data
public class VoteOption {
    /**
     * 选项ID
     */
    @TableId
    private Long id;

    /**
     * 所属投票ID
     */
    private Long voteId;

    /**
     * 选项内容
     */
    private String content;

    /**
     * 投票数（可选，实际可由统计得出）
     */
    private Integer voteCount;

    /**
     * 
     */
    private Date createTime;
}