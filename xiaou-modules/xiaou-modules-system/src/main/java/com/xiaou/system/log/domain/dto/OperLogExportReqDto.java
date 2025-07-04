package com.xiaou.system.log.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OperLogExportReqDto {
    private LocalDateTime beginTime;
    private LocalDateTime endTime;
}
