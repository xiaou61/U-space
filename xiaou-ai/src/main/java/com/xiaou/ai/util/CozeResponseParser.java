package com.xiaou.ai.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Coze响应解析工具
 * 统一处理Coze工作流返回的数据格式
 *
 * @author xiaou
 */
@Slf4j
public class CozeResponseParser {

    /**
     * 解析Coze工作流响应
     * Coze返回格式: {"output": "{...实际JSON...}"}
     * 需要先取出output字段，再二次解析
     *
     * @param response Coze原始响应
     * @return 解析后的JSONObject，解析失败返回null
     */
    public static JSONObject parse(String response) {
        if (StrUtil.isBlank(response)) {
            log.warn("Coze响应为空");
            return null;
        }

        try {
            JSONObject json = JSONUtil.parseObj(response);

            // 检查是否有output包装
            if (json.containsKey("output")) {
                String outputStr = json.getStr("output");
                if (StrUtil.isNotBlank(outputStr)) {
                    // 二次解析output内容
                    return JSONUtil.parseObj(outputStr);
                }
            }

            // 如果没有output包装，直接返回
            return json;

        } catch (Exception e) {
            log.error("解析Coze响应失败: {}", response, e);
            return null;
        }
    }

    /**
     * 安全获取字符串字段
     *
     * @param json     JSON对象
     * @param key      字段名
     * @param fallback 默认值
     * @return 字段值或默认值
     */
    public static String getString(JSONObject json, String key, String fallback) {
        if (json == null) {
            return fallback;
        }
        String value = json.getStr(key);
        return StrUtil.isNotBlank(value) ? value : fallback;
    }

    /**
     * 安全获取整数字段
     *
     * @param json     JSON对象
     * @param key      字段名
     * @param fallback 默认值
     * @return 字段值或默认值
     */
    public static Integer getInt(JSONObject json, String key, Integer fallback) {
        if (json == null) {
            return fallback;
        }
        return json.getInt(key, fallback);
    }

    /**
     * 检查响应是否包含错误
     *
     * @param response Coze原始响应
     * @return 是否为错误响应
     */
    public static boolean isErrorResponse(String response) {
        if (StrUtil.isBlank(response)) {
            return true;
        }
        return response.contains("[ERROR]") || response.contains("Workflow not found");
    }
}
