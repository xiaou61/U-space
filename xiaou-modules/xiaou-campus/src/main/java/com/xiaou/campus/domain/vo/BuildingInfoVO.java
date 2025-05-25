package com.xiaou.campus.domain.vo;

import com.alibaba.fastjson2.JSON;
import com.xiaou.campus.domain.entity.BuildingInfo;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class BuildingInfoVO {

    private Long id;

    private String name;

    private String aliases;

    private String img;

    private List<String> images;

    private String description;

    private Double latitude;

    private Double longitude;

    private String createBy;

    private Long categoryId;

    //vo额外字段需要处理
    private String categoryName;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;

    /**
     * 实体类转 VO
     */
    public static BuildingInfoVO fromEntity(BuildingInfo entity,String categoryName) {
        BuildingInfoVO vo = new BuildingInfoVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setAliases(entity.getAliases());
        vo.setImg(entity.getImg());
        vo.setDescription(entity.getDescription());
        vo.setLatitude(entity.getLatitude());
        vo.setLongitude(entity.getLongitude());
        vo.setCategoryId(entity.getCategoryId());
        vo.setCreateBy(entity.getCreateBy());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateBy(entity.getUpdateBy());
        vo.setUpdateTime(entity.getUpdateTime());
        vo.setCategoryName(categoryName);
        if (entity.getImages() != null) {
            vo.setImages(JSON.parseArray(entity.getImages(), String.class));
        }
        // categoryName没有设置
        return vo;
    }

}
