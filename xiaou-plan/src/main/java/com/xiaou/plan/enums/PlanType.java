package com.xiaou.plan.enums;

/**
 * 计划类型枚举
 * 
 * @author xiaou
 */
public enum PlanType {
    
    CODING(1, "刷题计划", "💻"),
    STUDY(2, "学习计划", "📚"),
    READING(3, "阅读计划", "📖"),
    EXERCISE(4, "运动计划", "🏃"),
    CUSTOM(5, "自定义", "✏️");
    
    private final int code;
    private final String desc;
    private final String icon;
    
    PlanType(int code, String desc, String icon) {
        this.code = code;
        this.desc = desc;
        this.icon = icon;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getDesc() {
        return desc;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public static PlanType fromCode(int code) {
        for (PlanType type : values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return CUSTOM;
    }
}
