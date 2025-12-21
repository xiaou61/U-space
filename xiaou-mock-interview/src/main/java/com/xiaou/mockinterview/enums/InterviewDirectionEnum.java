package com.xiaou.mockinterview.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 面试方向枚举
 *
 * @author xiaou
 */
@Getter
@AllArgsConstructor
public enum InterviewDirectionEnum {

    JAVA("java", "Java 后端", "☕"),
    FRONTEND("frontend", "前端开发", "🌐"),
    PYTHON("python", "Python 开发", "🐍"),
    GO("go", "Go 开发", "🔷"),
    FULLSTACK("fullstack", "全栈开发", "🔄"),
    DATABASE("database", "数据库", "🗄️"),
    DEVOPS("devops", "DevOps", "🔧"),
    ALGORITHM("algorithm", "算法", "🧮");

    /**
     * 方向代码
     */
    private final String code;

    /**
     * 方向名称
     */
    private final String name;

    /**
     * 图标
     */
    private final String icon;

    /**
     * 根据代码获取枚举
     */
    public static InterviewDirectionEnum getByCode(String code) {
        for (InterviewDirectionEnum direction : values()) {
            if (direction.getCode().equals(code)) {
                return direction;
            }
        }
        return null;
    }
}
