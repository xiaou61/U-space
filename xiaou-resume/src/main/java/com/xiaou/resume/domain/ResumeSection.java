package com.xiaou.resume.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 简历模块内容
 */
@Data
@Accessors(chain = true)
public class ResumeSection {

    private Long id;
    private Long resumeId;
    private String sectionType;
    private String title;
    private String content;
    private Integer sortOrder;
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
