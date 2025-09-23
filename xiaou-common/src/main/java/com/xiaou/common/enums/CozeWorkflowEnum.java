package com.xiaou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Coze工作流枚举
 * 统一管理所有Coze工作流的配置信息
 *
 * @author xiaou
 */
@Getter
@AllArgsConstructor
public enum CozeWorkflowEnum {
    
    /**
     * 示例工作流 - 文本生成
     */
    TEST("7553122464865255463", "测试工作流", "test");

    /**
     * 工作流ID
     */
    private final String workflowId;
    
    /**
     * 工作流名称
     */
    private final String workflowName;
    
    /**
     * 工作流描述
     */
    private final String description;
    
    /**
     * 根据工作流ID获取枚举
     *
     * @param workflowId 工作流ID
     * @return 工作流枚举，未找到时返回null
     */
    public static CozeWorkflowEnum getByWorkflowId(String workflowId) {
        for (CozeWorkflowEnum workflow : values()) {
            if (workflow.getWorkflowId().equals(workflowId)) {
                return workflow;
            }
        }
        return null;
    }
    
    /**
     * 检查工作流ID是否存在
     *
     * @param workflowId 工作流ID
     * @return 是否存在
     */
    public static boolean exists(String workflowId) {
        return getByWorkflowId(workflowId) != null;
    }
}
