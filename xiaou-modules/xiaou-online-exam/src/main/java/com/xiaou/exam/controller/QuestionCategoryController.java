package com.xiaou.exam.controller;

import com.xiaou.common.domain.R;
import com.xiaou.exam.domain.bo.QuestionCategoryBo;
import com.xiaou.exam.domain.vo.QuestionCategoryVo;
import com.xiaou.exam.service.QuestionCategoryService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/question/category")
@Validated
public class QuestionCategoryController {
    @Resource
    private QuestionCategoryService categoryService;

    @PostMapping
    public R<String> addCategory(@RequestBody QuestionCategoryBo category) {
        return categoryService.addCategory(category);
    }

    @PostMapping("/update/{id}")
    public R<String> updateCategory(@RequestBody QuestionCategoryBo category, @PathVariable Long id) {
        return categoryService.updateCategory(category, id);
    }

    @PostMapping("/delete/{id}")
    public R<String> deleteCategory(@PathVariable Long id) {
        return categoryService.deleteCategory(id);
    }

    /**
     * 获得分类树
     *
     * @return
     */
    @GetMapping("/tree")
    public R<List<QuestionCategoryVo>> getCategoryTree() {
        return categoryService.getCategoryTree();
    }
    /**
     * 获得一级分类
     */
    @GetMapping("/first")
    public R<List<QuestionCategoryVo>> getFirstLevelCategories() {
        return categoryService.getFirstLevelCategories();
    }
    /**
     * 获得子分类
     */
    @GetMapping("/children/{id}")
    public R<List<QuestionCategoryVo>> getChildrenCategories(@PathVariable Long id) {
        return categoryService.getChildrenCategories(id);
    }
}
