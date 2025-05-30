package com.xiaou.campus.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CategoryWithBuildingListVO {
    private Long id;
    private String name;
    private List<BuildingInfoVO> list;
}
