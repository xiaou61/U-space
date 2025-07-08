package com.xiaou.bbs.domain.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * 帖子分类表
 * @TableName u_bbs_category
 */
@TableName(value ="u_bbs_category")
@Data
public class BbsCategory {

    /**
     * 分类ID，主键 UUID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 分类名称，如 学习、生活、娱乐等
     */
    private String name;

    /**
     * 分类状态，1启用 0禁用
     */
    private Integer status;

    /**
     * 逻辑删除标志，0正常 1已删除
     */
    @TableLogic
    private Integer isDeleted;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedAt;
}