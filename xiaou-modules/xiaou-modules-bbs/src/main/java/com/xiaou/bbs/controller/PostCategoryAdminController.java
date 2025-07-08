package com.xiaou.bbs.controller;

import com.xiaou.bbs.domain.req.BbsCategoryReq;
import com.xiaou.bbs.domain.resp.BbsCategoryResp;
import com.xiaou.bbs.service.BbsCategoryService;
import com.xiaou.common.domain.R;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/post-categeteory")
@Validated
public class PostCategoryAdminController {
    @Resource
    private BbsCategoryService categoryService;

    /**
     * 添加一个分类
     */
    @PostMapping("/add")
    public R<String> addCategory(@RequestBody BbsCategoryReq req){
        return categoryService.addCategory(req);
    }
    /**
     * 删除一个分类
     */
    @PostMapping("/delete")
    public R<String> deleteCategory(@RequestBody String id){
        return categoryService.deleteCategory(id);
    }
    /**
     * 修改一个分类名称
     */
    @PostMapping("/update")
    public R<String> updateCategory(@RequestParam String id , @RequestBody BbsCategoryReq req){
        return categoryService.updateCategory(id,req);
    }
    /**
     * 列出所有的分类
     */
    @GetMapping("/list")
    public R<List<BbsCategoryResp>> listCategory(){
        return categoryService.listCategory();
    }
}
