package com.xiaou.codepen.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * CodePen 模块配置属性
 * 
 * @author xiaou
 */
@Data
@Component
@ConfigurationProperties(prefix = "codepen")
public class CodePenProperties {
    
    /**
     * 分享链接基础URL
     */
    private String shareBaseUrl = "https://code-nest.com/pen/";
    
    /**
     * 创建作品奖励积分
     */
    private int createRewardPoints = 10;
}
