package com.xiaou.ai.tools;

import org.springframework.ai.tool.annotation.Tool;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 提供当前服务器时间的工具。
 */
public class TimeTool {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Tool(description = "获取当前服务器时间（yyyy-MM-dd HH:mm:ss）")
    public String now() {
        return LocalDateTime.now().format(FORMATTER);
    }
}