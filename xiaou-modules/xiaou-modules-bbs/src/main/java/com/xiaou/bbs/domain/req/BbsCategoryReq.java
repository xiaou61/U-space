package com.xiaou.bbs.domain.req;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaou.bbs.domain.entity.BbsCategory;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.util.Date;

/**
 * 帖子分类表
 * @TableName u_bbs_category
 */
@Data
@AutoMapper(target = BbsCategory.class)
public class BbsCategoryReq {

    /**
     * 分类名称，如 学习、生活、娱乐等
     */
    private String name;

    /**
     * 分类状态，1启用 0禁用
     */
    private Integer status;

}