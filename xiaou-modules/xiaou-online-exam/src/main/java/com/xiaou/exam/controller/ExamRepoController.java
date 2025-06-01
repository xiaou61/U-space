package com.xiaou.exam.controller;

import com.xiaou.common.domain.R;
import com.xiaou.exam.domain.bo.ExamRepoBo;
import com.xiaou.exam.domain.vo.ExamRepoVo;
import com.xiaou.exam.service.ExamRepoService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.xnio.Result;

import java.util.List;

@RestController
@RequestMapping("/question/repo")
@Validated
public class ExamRepoController {
    @Resource
    private ExamRepoService examRepoService;

    /**
     * 添加题库
     */
    @PostMapping("/add")
    public R<String> addRepo(@Validated @RequestBody ExamRepoBo examRepoBo) {
        return examRepoService.addRepo(examRepoBo);
    }
    /**
     * 修改题库
     */
    @PostMapping("/update/{id}")
    public R<String> updateRepo(@Validated @RequestBody Long id, @RequestBody ExamRepoBo examRepoBo) {
        return examRepoService.updateRepo(id,examRepoBo);
    }
    /**
     * 删除题库
     */
    @PostMapping("/delete/{id}")
    public R<String> deleteRepo(@PathVariable Long id) {
        return examRepoService.deleteRepo(id);
    }
    /**
     * 根据分类ID查询题库
     */
    @GetMapping("/list/{categoryId}")
    public R<List<ExamRepoVo> >getRepoByCategoryId(@PathVariable Long categoryId) {
        return examRepoService.getRepoByCategoryId(categoryId);
    }
}
