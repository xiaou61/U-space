package com.xiaou.resume.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 简历模板实体
 */
@Data
@Accessors(chain = true)
public class ResumeTemplate {

    private Long id;
    private String name;
    private String category;
    private String description;
    private String coverUrl;
    private String previewUrl;
    private String tags;
    private String techStack;
    private Integer experienceLevel;
    private Double rating;
    private Integer ratingCount;
    private Integer downloadCount;
    private Integer status;
    private Long createdBy;
    private Long updatedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
