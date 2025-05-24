package com.xiaou.campus.controller;

import com.xiaou.campus.domain.bo.PlaceCategoryBO;
import com.xiaou.campus.domain.vo.PlaceCategoryVO;
import com.xiaou.campus.service.PlaceCategoryService;
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
@RequestMapping("/place/category")
@Validated
public class PlaceCategoryController {
    @Resource
    private PlaceCategoryService placeCategoryService;

    /**
     * 新增分类
     */
    @Log(title = "新增地点分类", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public R<PlaceCategoryVO> addCategory(@RequestBody @Valid PlaceCategoryBO bo) {
        return placeCategoryService.addCategory(bo);
    }

    /**
     * 删除分类
     */
    @Log(title = "删除地点分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public R<Boolean> deleteCategory(@PathVariable Long id) {
        return placeCategoryService.deleteCategory(id);
    }

    /**
     * 修改分类
     */
    @Log(title = "修改地点分类", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public R<PlaceCategoryVO> updateCategory(@PathVariable Long id, @RequestBody @Valid PlaceCategoryBO bo) {
        return placeCategoryService.updateCategory(id,bo);
    }

    /**
     * 分页查询地点分类
     */
    @GetMapping("/list")
    public R<PageRespDto<PlaceCategoryVO>> list(@RequestBody PageReqDto req) {
        return placeCategoryService.list(req);
    }


}
