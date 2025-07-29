package com.xiaou.subject.domain.req;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 抢课请求类
 */
@Data
public class CourseGrabReq {
    /**
     * 课程ID
     */
    @NotBlank(message = "课程ID不能为空")
    private String courseId;
}