package com.xiaou.userinfo.controller;

import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.log.annotation.Log;
import com.xiaou.log.enums.BusinessType;
import com.xiaou.userinfo.domain.bo.UClassBO;
import com.xiaou.userinfo.domain.vo.UClassVO;
import com.xiaou.userinfo.service.ClassService;
import jakarta.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/class")
@Validated
public class ClassController {

    @Resource
    private ClassService classService;

    /**
     * 添加班级
     */
    @Log(title = "添加班级", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public R<UClassVO> addClass(@RequestBody  UClassBO classBO) {
        return classService.addClass(classBO);
    }

    /**
     * 修改班级
     */
    @Log(title = "修改班级", businessType = BusinessType.UPDATE)
    @PutMapping("/update/{id}")
    public R<UClassVO> updateClass(
            @PathVariable("id") Long id,
            @RequestBody UClassBO classBO) {
        return classService.updateClass(id, classBO);
    }

    /**
     * 删除班级
     */
    @Log(title = "删除班级", businessType = BusinessType.DELETE)
    @DeleteMapping("/delete/{id}")
    public R<Void> deleteClass(@PathVariable("id") Long id) {
        return classService.deleteClass(id);
    }

    /**
     * 分页查询班级
     */
    @PostMapping("/page")
    public R<PageRespDto<UClassVO>> allClassPage(@RequestBody PageReqDto pageReqDto) {
        return classService.allClassPage(pageReqDto);
    }


}
