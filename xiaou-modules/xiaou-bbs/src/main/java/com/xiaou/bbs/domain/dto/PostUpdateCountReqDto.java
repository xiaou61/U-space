package com.xiaou.bbs.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostUpdateCountReqDto {
    private LocalDateTime lastRefreshTime;  // 用户上次刷新时间
}
