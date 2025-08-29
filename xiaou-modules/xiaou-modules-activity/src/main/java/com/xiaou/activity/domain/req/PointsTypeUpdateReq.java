package com.xiaou.activity.domain.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新积分类型请求
 */
@Data
public class PointsTypeUpdateReq {

    /**
     * 积分类型名称
     */
    @NotBlank(message = "积分类型名称不能为空")
    private String typeName;

    /**
     * 积分类型编码
     */
    @NotBlank(message = "积分类型编码不能为空")
    private String typeCode;

    /**
     * 积分类型描述
     */
    private String description;

    /**
     * 积分图标URL
     */
    private String iconUrl;

    /**
     * 排序顺序
     */
    private Integer sortOrder;
} 