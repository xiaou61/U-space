package com.xiaou.campus.controller;

import com.xiaou.campus.domain.bo.PlaceCategoryBO;
import com.xiaou.campus.domain.entity.QaPairs;
import com.xiaou.campus.service.QaPairsService;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/qa")
@Validated
public class QaPairsController {
    @Resource
    private QaPairsService qaPairsService;

    /**
     * 新增qa分类
     */
    @PostMapping("/add")
    public R<QaPairs> addQA(@RequestBody @Validated QaPairs qaPairs) {
        return qaPairsService.addQA(qaPairs);
    }
    /**
     * 删除qa分类
     */
    @DeleteMapping("/delete")
    public R<QaPairs> deleteQA(@PathVariable Long id) {
        return qaPairsService.deleteQA(id);
    }
    /**
     * 修改qa分类
     */
    @PutMapping("/update")
    public R<QaPairs> updateQA(@RequestBody @Validated QaPairs qaPairs) {
        return qaPairsService.updateQA(qaPairs);
    }

    /**
     * qa分页
     */
    @PostMapping("/list")
    public R<PageRespDto<QaPairs>> list(@RequestBody PageReqDto dto) {
        return  qaPairsService.allQaPage(dto);
    }
}
