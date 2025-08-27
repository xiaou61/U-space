package com.xiaou.activity.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 投票选项表
 * @TableName u_vote_option
 */
@TableName(value ="u_vote_option")
@Data
public class VoteOption {
    /**
     * 选项ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 活动ID
     */
    private String activityId;

    /**
     * 选项名称
     */
    private String optionName;

    /**
     * 选项描述
     */
    private String optionDesc;

    /**
     * 选项图片
     */
    private String imageUrl;

    /**
     * 票数
     */
    private Integer voteCount;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateAt;
}