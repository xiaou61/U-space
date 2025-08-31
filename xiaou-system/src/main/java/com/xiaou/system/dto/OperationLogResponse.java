package com.xiaou.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志响应DTO
 *
 * @author xiaou
 */
@Data
@Schema(description = "操作日志信息")
public class OperationLogResponse {

    /**
     * 日志ID
     */
    @Schema(description = "日志ID", example = "1")
    private Long id;

    /**
     * 操作ID
     */
    @Schema(description = "操作ID", example = "OP001")
    private String operationId;

    /**
     * 操作模块
     */
    @Schema(description = "操作模块", example = "用户管理")
    private String module;

    /**
     * 操作类型
     */
    @Schema(description = "操作类型", example = "UPDATE")
    private String operationType;

    /**
     * 操作类型描述
     */
    @Schema(description = "操作类型描述", example = "修改")
    private String operationTypeText;

    /**
     * 操作描述
     */
    @Schema(description = "操作描述", example = "更新个人信息")
    private String description;

    /**
     * 请求方法
     */
    @Schema(description = "请求方法", example = "updateProfile")
    private String method;

    /**
     * 请求URI
     */
    @Schema(description = "请求URI", example = "/auth/profile")
    private String requestUri;

    /**
     * HTTP请求方法
     */
    @Schema(description = "HTTP请求方法", example = "PUT")
    private String requestMethod;

    /**
     * 请求参数
     */
    @Schema(description = "请求参数")
    private String requestParams;

    /**
     * 响应数据
     */
    @Schema(description = "响应数据")
    private String responseData;

    /**
     * 操作人ID
     */
    @Schema(description = "操作人ID", example = "1")
    private Long operatorId;

    /**
     * 操作人姓名
     */
    @Schema(description = "操作人姓名", example = "admin")
    private String operatorName;

    /**
     * 操作IP
     */
    @Schema(description = "操作IP", example = "127.0.0.1")
    private String operatorIp;

    /**
     * 操作地点
     */
    @Schema(description = "操作地点", example = "本地")
    private String operationLocation;

    /**
     * 浏览器类型
     */
    @Schema(description = "浏览器类型", example = "Chrome 120.0.0.0")
    private String browser;

    /**
     * 操作系统
     */
    @Schema(description = "操作系统", example = "Windows 10")
    private String os;

    /**
     * 操作状态：0-成功，1-失败
     */
    @Schema(description = "操作状态：0-成功，1-失败", example = "0")
    private Integer status;

    /**
     * 操作状态描述
     */
    @Schema(description = "操作状态描述", example = "成功")
    private String statusText;

    /**
     * 错误消息
     */
    @Schema(description = "错误消息")
    private String errorMsg;

    /**
     * 操作时间
     */
    @Schema(description = "操作时间", example = "2024-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime operationTime;

    /**
     * 耗时（毫秒）
     */
    @Schema(description = "耗时（毫秒）", example = "125")
    private Long costTime;

    /**
     * 设置操作状态描述
     */
    public void setStatus(Integer status) {
        this.status = status;
        this.statusText = status != null && status == 0 ? "成功" : "失败";
    }

    /**
     * 设置操作类型描述
     */
    public void setOperationType(String operationType) {
        this.operationType = operationType;
        this.operationTypeText = getOperationTypeText(operationType);
    }

    private String getOperationTypeText(String operationType) {
        if (operationType == null) return "";
        switch (operationType) {
            case "SELECT": return "查询";
            case "INSERT": return "新增";
            case "UPDATE": return "修改";
            case "DELETE": return "删除";
            case "GRANT": return "授权";
            case "EXPORT": return "导出";
            case "IMPORT": return "导入";
            case "FORCE": return "强退";
            case "GENCODE": return "生成代码";
            case "CLEAN": return "清空数据";
            default: return "其它";
        }
    }
} 