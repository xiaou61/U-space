package com.xiaou.interview.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;



/**
 * 掌握度标记请求
 *
 * @author xiaou
 */
@Data
public class MasteryMarkRequest {

    /**
     * 题目ID
     */
    @NotNull(message = "题目ID不能为空")
    private Long questionId;

    /**
     * 题单ID
     */
    @NotNull(message = "题单ID不能为空")
    private Long questionSetId;

    /**
     * 掌握度等级 1-不会 2-模糊 3-熟悉 4-已掌握
     */
    @NotNull(message = "掌握度等级不能为空")
    @Min(value = 1, message = "掌握度等级最小为1")
    @Max(value = 4, message = "掌握度等级最大为4")
    private Integer masteryLevel;

    /**
     * 是否复习（非首次学习）
     */
    private Boolean isReview = false;
}
