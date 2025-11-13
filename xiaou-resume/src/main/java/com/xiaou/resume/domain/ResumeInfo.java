package com.xiaou.resume.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 简历主体信息
 */
@Data
@Accessors(chain = true)
public class ResumeInfo {

    private Long id;
    private Long userId;
    private String resumeName;
    private Long templateId;
    private String summary;
    private Integer status;
    private Integer visibility;
    private Integer version;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
