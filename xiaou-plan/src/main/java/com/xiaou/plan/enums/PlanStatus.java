package com.xiaou.plan.enums;

/**
 * 计划状态枚举
 * 
 * @author xiaou
 */
public enum PlanStatus {
    
    DELETED(0, "已删除"),
    ACTIVE(1, "进行中"),
    PAUSED(2, "已暂停"),
    COMPLETED(3, "已完成"),
    EXPIRED(4, "已过期");
    
    private final int code;
    private final String desc;
    
    PlanStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getDesc() {
        return desc;
    }
    
    public static PlanStatus fromCode(int code) {
        for (PlanStatus status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return ACTIVE;
    }
}
