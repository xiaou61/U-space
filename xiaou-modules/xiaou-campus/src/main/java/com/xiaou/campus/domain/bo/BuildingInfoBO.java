package com.xiaou.campus.domain.bo;

import com.alibaba.fastjson2.JSON;
import com.xiaou.campus.domain.entity.BuildingInfo;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class BuildingInfoBO {

    @NotBlank(message = "建筑名称不能为空")
    @Size(max = 100, message = "建筑名称不能超过100个字符")
    private String name;

    @Size(max = 255, message = "别名不能超过255个字符")
    private String aliases;

    @NotBlank(message = "全景图 URL 不能为空")
    @Size(max = 1024, message = "全景图 URL 长度不能超过1024字符")
    @Pattern(regexp = "^(http|https)://.*$", message = "全景图 URL 必须是合法的 HTTP(S) 链接")
    private String img;

    @NotNull(message = "详情图列表不能为空")
    @Size(min = 1, message = "至少需要一张详情图")
    private List<@Pattern(regexp = "^(http|https)://.*$", message = "详情图 URL 必须是合法的 HTTP(S) 链接") String> images;

    @NotBlank(message = "描述信息不能为空")
    @Size(max = 1000, message = "描述信息不能超过1000个字符")
    private String description;

    @NotNull(message = "纬度不能为空")
    @DecimalMin(value = "-90.0", message = "纬度不能小于 -90")
    @DecimalMax(value = "90.0", message = "纬度不能大于 90")
    private Double latitude;

    @NotNull(message = "经度不能为空")
    @DecimalMin(value = "-180.0", message = "经度不能小于 -180")
    @DecimalMax(value = "180.0", message = "经度不能大于 180")
    private Double longitude;

    private Long categoryId;

    /**
     * 转为实体 BuildingInfo
     */
    public BuildingInfo toEntity() {
        BuildingInfo entity = new BuildingInfo();
        // 不设 id，由数据库自增
        entity.setName(this.name);
        entity.setAliases(this.aliases);
        entity.setImg(this.img);
        entity.setDescription(this.description);
        entity.setLatitude(this.latitude);
        entity.setLongitude(this.longitude);
        entity.setCategoryId(this.categoryId);
        if (this.images != null) {
            entity.setImages(JSON.toJSONString(this.images));
        }
        return entity;
    }
}
