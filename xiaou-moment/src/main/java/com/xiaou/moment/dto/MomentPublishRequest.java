package com.xiaou.moment.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.List;

/**
 * 发布动态请求
 */
@Data
public class MomentPublishRequest {
    
    /**
     * 动态内容
     */
    @NotBlank(message = "动态内容不能为空")
    @Length(min = 1, max = 100, message = "动态内容长度需在1-100字符之间")
    private String content;
    
    /**
     * 图片URLs
     */
    @Valid
    private List<String> images;
    
    /**
     * 验证图片数量
     */
    @AssertTrue(message = "图片数量不能超过9张")
    public boolean isValidImageCount() {
        return images == null || images.size() <= 9;
    }
} 