package com.xiaou.moyu.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

/**
 * 工作时间操作请求对象
 *
 * @author xiaou
 */
@Data
public class WorkTimeRequest {
    
    /**
     * 操作类型：START-开始工作，END-结束工作，PAUSE-暂停工作，RESUME-恢复工作
     */
    @NotNull(message = "操作类型不能为空")
    private String action;
    
    /**
     * 备注信息
     */
    private String remark;
}
