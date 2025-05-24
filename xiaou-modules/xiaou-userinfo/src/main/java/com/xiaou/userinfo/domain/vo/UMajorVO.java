package com.xiaou.userinfo.domain.vo;

import com.xiaou.userinfo.domain.entity.Major;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UMajorVO {
    private Long majorId;
    private String name;
    private String code;
    private Long collegeId;
    private String leader;
    private String educationLevel;
    private String status;
    private String collegeName; // 新增字段
    private String createdBy;        // 创建人
    private String updatedBy;        // 更新人
    private LocalDateTime createdTime;  // 创建时间
    private LocalDateTime updatedTime;  // 更新时间

    public static UMajorVO fromEntity(Major major, String collegeName) {
        return UMajorVO.builder()
                .majorId(major.getMajorId())
                .name(major.getName())
                .code(major.getCode())
                .collegeId(major.getCollegeId())
                .leader(major.getLeader())
                .educationLevel(major.getEducationLevel())
                .status(major.getStatus())
                .collegeName(collegeName)
                .createdBy(major.getCreatedBy())
                .updatedBy(major.getUpdatedBy())
                .createdTime(major.getCreatedTime())
                .updatedTime(major.getUpdatedTime())
                .build();
    }
}
