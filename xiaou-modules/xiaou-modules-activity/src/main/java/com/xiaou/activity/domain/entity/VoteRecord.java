package com.xiaou.activity.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 投票记录表
 *
 * @TableName u_vote_record
 */
@TableName(value = "u_vote_record")
@Data
public class VoteRecord {
    /**
     * 记录ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 活动ID
     */
    private String activityId;

    /**
     * 选项ID
     */
    private String optionId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 投票时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date voteTime;
}