package com.xiaou.campus.controller;

import com.xiaou.campus.domain.bo.CampusGuideBO;
import com.xiaou.campus.domain.entity.CampusGuide;
import com.xiaou.campus.domain.vo.CampusGuideVO;
import com.xiaou.campus.service.CampusGuideService;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/campus/guide")
@Validated
public class CampusGuideController {

    @Resource
    private CampusGuideService campusGuideService;

    /**
     * 新增校园指南
     */
    @PostMapping("/add")
    public R<CampusGuideVO> addGuide(@RequestBody CampusGuideBO campusGuideBO) {
        return campusGuideService.addGuide(campusGuideBO);
    }
    /**
     * 更新校园指南
     */
    @PutMapping("/update/{id}")
    public R<CampusGuideVO> updateGuide(@PathVariable Long id, @RequestBody CampusGuideBO campusGuideBO) {
        return campusGuideService.updateGuide(id, campusGuideBO);
    }

    /**
     * 删除校园指南（逻辑删除）
     */
    @DeleteMapping("/delete/{id}")
    public R<String> deleteGuide(@PathVariable Long id) {
        return campusGuideService.deleteGuide(id);
    }

    /**
     * 分页查询校园指南
     */
    @PostMapping("/list")
    public R<PageRespDto<CampusGuideVO>> allGuidePage(@RequestBody PageReqDto pageReqDto) {
        return campusGuideService.allGuidePage(pageReqDto);
    }
}
