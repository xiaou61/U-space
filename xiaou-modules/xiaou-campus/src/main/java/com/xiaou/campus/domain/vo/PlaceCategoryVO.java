package com.xiaou.campus.domain.vo;

import com.xiaou.campus.domain.entity.PlaceCategory;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AutoMapper(target = PlaceCategory.class)
public class PlaceCategoryVO {

    private Long id;

    private String name;

    private LocalDateTime createTime;

    private String createBy;

    private LocalDateTime updateTime;

    private String updateBy;
}
