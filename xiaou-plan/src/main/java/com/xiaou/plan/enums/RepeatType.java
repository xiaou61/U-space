package com.xiaou.plan.enums;

/**
 * 重复类型枚举
 * 
 * @author xiaou
 */
public enum RepeatType {
    
    DAILY(1, "每日"),
    WORKDAY(2, "工作日"),
    WEEKEND(3, "周末"),
    CUSTOM(4, "自定义");
    
    private final int code;
    private final String desc;
    
    RepeatType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getDesc() {
        return desc;
    }
    
    public static RepeatType fromCode(int code) {
        for (RepeatType type : values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return DAILY;
    }
}
