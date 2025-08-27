package com.xiaou.activity.domain.req;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


import java.util.List;

/**
 * 积分发放请求
 */
@Data
public class PointsGrantReq {

    /**
     * 用户ID列表
     */
    @NotEmpty(message = "用户ID列表不能为空")
    private List<String> userIds;

    /**
     * 活动ID（可选）
     */
    private Long activityId;

    /**
     * 积分类型ID
     */
    @NotNull(message = "积分类型ID不能为空")
    private Long pointsTypeId;

    /**
     * 积分数量
     */
    @NotNull(message = "积分数量不能为空")
    @Min(value = 1, message = "积分数量必须大于0")
    private Integer pointsAmount;

    /**
     * 备注
     */
    private String remark;
} 