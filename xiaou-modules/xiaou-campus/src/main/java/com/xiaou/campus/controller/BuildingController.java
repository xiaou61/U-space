package com.xiaou.campus.controller;


import com.xiaou.campus.domain.bo.BuildingInfoBO;
import com.xiaou.campus.domain.bo.CampusGuideBO;
import com.xiaou.campus.domain.vo.BuildingInfoVO;
import com.xiaou.campus.domain.vo.CampusGuideVO;
import com.xiaou.campus.domain.vo.CategoryWithBuildingListVO;
import com.xiaou.campus.service.BuildingService;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.log.annotation.Log;
import com.xiaou.log.enums.BusinessType;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/campus/build")
@Validated
public class BuildingController {
    @Resource
    private BuildingService buildingService;

    /**
     * 新增建筑地点
     */
    @Log(title = "新增建筑地点", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public R<BuildingInfoVO> addGuide(@RequestBody BuildingInfoBO bo) {
        return buildingService.addBuild(bo);
    }


    @Log(title = "更新建筑地点", businessType = BusinessType.UPDATE)
    @PutMapping("/update/{id}")
    public R<BuildingInfoVO> updateBuilding(@PathVariable Long id,
                                            @Valid @RequestBody BuildingInfoBO buildingInfoBO) {
        return buildingService.updateBuild(id, buildingInfoBO);
    }

    @Log(title = "删除建筑地点", businessType = BusinessType.DELETE)
    @DeleteMapping("/delete/{id}")
    public R<String> deleteBuilding(@PathVariable Long id) {
        return buildingService.deleteBuild(id);
    }


    /**
     * 分页查询校园指南
     */
    @PostMapping("/list")
    public R<PageRespDto<CategoryWithBuildingListVO>> allGuidePage(@RequestBody PageReqDto pageReqDto) {
        return buildingService.allGuidePage(pageReqDto);
    }
}
