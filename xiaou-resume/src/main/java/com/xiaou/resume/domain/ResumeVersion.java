package com.xiaou.resume.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 简历版本快照
 */
@Data
@Accessors(chain = true)
public class ResumeVersion {

    private Long id;
    private Long resumeId;
    private Integer versionNumber;
    private String snapshot;
    private String changeLog;
    private Long createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
