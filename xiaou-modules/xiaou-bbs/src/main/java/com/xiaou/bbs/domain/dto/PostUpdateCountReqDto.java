package com.xiaou.bbs.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class PostUpdateCountReqDto {
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") // 仅限请求参数，如 query param
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8") // 用于 JSON
    private LocalDateTime lastRefreshTime;  // 用户上次刷新时间
}
