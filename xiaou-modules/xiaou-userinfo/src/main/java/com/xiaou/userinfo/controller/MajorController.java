package com.xiaou.userinfo.controller;

import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.log.annotation.Log;
import com.xiaou.log.enums.BusinessType;
import com.xiaou.userinfo.domain.bo.UMajorBO;
import com.xiaou.userinfo.domain.entity.Major;
import com.xiaou.userinfo.domain.vo.UCollegeVO;
import com.xiaou.userinfo.domain.vo.UMajorVO;
import com.xiaou.userinfo.service.MajorService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/major")
@Validated
public class MajorController {

    @Resource
    private MajorService majorService;

    /**
     * 添加专业
     */
    @Log(title = "添加专业", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public R<UMajorVO> addMajor(@RequestBody @Valid UMajorBO majorBO) {
        return majorService.addMajor(majorBO);
    }

    /**
     * 修改专业
     */
    @Log(title = "修改专业", businessType = BusinessType.UPDATE)
    @PutMapping("/update/{id}")
    public R<UMajorVO> updateMajor(
            @PathVariable("id") Long id,
            @RequestBody @Valid UMajorBO majorBO) {
        return majorService.updateMajor(id, majorBO);
    }

    /**
     * 删除专业
     */
    @Log(title = "删除专业", businessType = BusinessType.DELETE)
    @DeleteMapping("/delete/{id}")
    public R<Void> deleteMajor(@PathVariable("id") Long id) {
        return majorService.deleteMajor(id);
    }

    /**
     * 分页查询专业
     */

    @PostMapping("/page")
    public R<PageRespDto<UMajorVO>> allMajorPage(@RequestBody PageReqDto pageReqDto) {
        return majorService.allMajorPage(pageReqDto);
    }



}
