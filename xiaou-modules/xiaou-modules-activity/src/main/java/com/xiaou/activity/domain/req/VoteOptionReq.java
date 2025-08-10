package com.xiaou.activity.domain.req;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 投票选项表
 * @TableName u_vote_option
 */
@Data
public class VoteOptionReq {
    /**
     * 选项ID
     */
    private String id;

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

}