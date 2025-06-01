package com.xiaou.hot.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@TableName(value ="u_hot_post")
@Data
@Builder
public class HotPost {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 排行榜名称
     */
    @TableField(value = "name")
    private String name;

    /**
     *  热点类型名称
     */
    @TableField(value = "typeName")
    private String typeName;

    /**
     *  热点类型
     */
    @TableField(value = "type")
    private String type;

    /**
     * 图标地址
     */
    @TableField(value = "iconUrl")
    private String iconUrl;

    /**
     * 热点数据（json）
     */
    @TableField(value = "hostJson")
    private String hostJson;

    /**
     *  排序
     */
    @TableField(value = "sort")
    private Integer sort;

    /**
     * 分类
     */
    @TableField(value = "category")
    private Integer category;

    /**
     * 更新间隔，以小时为单位
     */
    @TableField(value = "updateInterval")
    private BigDecimal updateInterval;

    /**
     * 创建时间
     */
    @TableField(value = "createTime")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "updateTime")
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableField(value = "isDelete")
    private Integer isDelete;
}