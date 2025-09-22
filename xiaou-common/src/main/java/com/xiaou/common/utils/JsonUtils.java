package com.xiaou.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON工具类
 * 用于处理实体类中的JSON字段转换
 *
 * @author xiaou
 */
@Slf4j
public class JsonUtils {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 将字符串列表转换为JSON字符串
     *
     * @param list 字符串列表
     * @return JSON字符串
     */
    public static String listToJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            log.error("转换List到JSON失败", e);
            return null;
        }
    }
    
    /**
     * 将整数列表转换为JSON字符串
     *
     * @param list 整数列表
     * @return JSON字符串
     */
    public static String integerListToJson(List<Integer> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            log.error("转换Integer List到JSON失败", e);
            return null;
        }
    }
    
    /**
     * 将JSON字符串转换为字符串列表
     *
     * @param json JSON字符串
     * @return 字符串列表
     */
    public static List<String> jsonToStringList(String json) {
        if (!StringUtils.hasText(json)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.error("转换JSON到String List失败: {}", json, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 将JSON字符串转换为整数列表
     *
     * @param json JSON字符串
     * @return 整数列表
     */
    public static List<Integer> jsonToIntegerList(String json) {
        if (!StringUtils.hasText(json)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<Integer>>() {});
        } catch (JsonProcessingException e) {
            log.error("转换JSON到Integer List失败: {}", json, e);
            return new ArrayList<>();
        }
    }
}
