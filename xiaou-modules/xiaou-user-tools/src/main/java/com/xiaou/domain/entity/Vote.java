package com.xiaou.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName u_vote
 */
@TableName(value ="u_vote")
@Data
public class Vote {
    /**
     * 投票ID
     */
    @TableId
    private Long id;

    /**
     * 投票标题
     */
    private String title;

    /**
     * 投票描述
     */
    private String description;

    /**
     * 是否多选
     */
    private Integer isMultiple;

    /**
     * 是否匿名投票
     */
    private Integer isAnonymous;

    /**
     * 最多可选项数量（仅在多选时生效）
     */
    private Integer maxChoices;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 创建者用户ID
     */
    private Long creatorId;

    /**
     * 状态（1启用，0关闭）
     */
    private Integer status;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;
}