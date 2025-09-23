package com.xiaou.moyu.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

/**
 * 灵活的日期反序列化器，支持多种日期格式
 * 包括标准格式 yyyy-MM-dd HH:mm:ss 和 ISO 8601 格式
 *
 * @author xiaou
 */
@Slf4j
public class FlexibleDateDeserializer extends JsonDeserializer<Date> {
    
    private static final String STANDARD_PATTERN = "yyyy-MM-dd HH:mm:ss";
    
    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateString = p.getValueAsString();
        
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        
        // 尝试标准格式
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(STANDARD_PATTERN);
            return sdf.parse(dateString);
        } catch (ParseException e) {
            // 忽略，尝试其他格式
        }
        
        // 尝试ISO 8601格式
        try {
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            return Date.from(offsetDateTime.toInstant());
        } catch (DateTimeParseException e) {
            // 忽略，尝试其他格式
        }
        
        // 尝试解析为Instant（兼容其他ISO格式）
        try {
            Instant instant = Instant.parse(dateString);
            return Date.from(instant);
        } catch (DateTimeParseException e) {
            log.warn("无法解析日期字符串: {}", dateString);
            return new Date(); // 返回当前时间作为默认值
        }
    }
}
