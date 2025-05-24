package com.xiaou.campus.domain.bo;

import com.xiaou.campus.domain.entity.CampusGuide;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@AutoMapper(target = CampusGuide.class)
public class CampusGuideBO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题长度不能超过100")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    /**
     * 图片 URL 列表
     */
    private List<String> imageList;

    /**
     * 关键词列表
     */
    private List<String> keywords;

    private String category;

}
