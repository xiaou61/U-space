package com.xiaou.sensitive.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 敏感词版本历史实体
 *
 * @author xiaou
 */
@Data
public class SensitiveVersion {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 版本号（如v1.0.1）
     */
    private String versionNo;

    /**
     * 变更类型（add/update/delete/import）
     */
    private String changeType;

    /**
     * 变更数量
     */
    private Integer changeCount;

    /**
     * 变更详情（JSON格式）
     */
    private String changeDetail;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 备注
     */
    private String remark;
}
