package com.xiaou.knowledge.dto.request;

import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 节点排序请求
 * 
 * @author xiaou
 */
@Data
public class SortNodesRequest {
    
    /**
     * 节点排序信息列表
     */
    @NotEmpty(message = "节点排序信息不能为空")
    @Valid
    private List<NodeOrder> nodeOrders;
    
    /**
     * 节点排序信息
     */
    @Data
    public static class NodeOrder {
        /**
         * 节点ID
         */
        @NotNull(message = "节点ID不能为空")
        private Long nodeId;
        
        /**
         * 父节点ID
         */
        @NotNull(message = "父节点ID不能为空")
        private Long parentId;
        
        /**
         * 排序序号
         */
        @NotNull(message = "排序序号不能为空")
        private Integer sortOrder;
    }
} 