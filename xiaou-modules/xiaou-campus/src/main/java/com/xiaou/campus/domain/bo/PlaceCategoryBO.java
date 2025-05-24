package com.xiaou.campus.domain.bo;

import com.xiaou.campus.domain.entity.PlaceCategory;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@AutoMapper(target = PlaceCategory.class)
public class PlaceCategoryBO {


    @NotBlank(message = "分类名称不能为空")
    @Size(max = 100, message = "分类名称长度不能超过100字符")
    private String name;
}
