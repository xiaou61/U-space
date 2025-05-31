package com.xiaou.exam.controller;

import com.xiaou.common.domain.R;
import com.xiaou.exam.domain.bo.QuestionBo;
import com.xiaou.exam.domain.entity.Question;
import com.xiaou.exam.domain.vo.QuestionVo;
import com.xiaou.exam.service.QuestionService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/question")
@Validated
public class QuestionController {
    @Resource
    private QuestionService questionService;

    /**
     * 添加试题
     */
    @PostMapping("/add")
    public R<String> addQuestion(@RequestBody QuestionBo questionBo) {
        return questionService.addQuestion(questionBo);
    }

    /**
     * 批量删除试题
     */
    @DeleteMapping("/delete")
    public R<String> deleteQuestions(@RequestBody String ids) {
        return questionService.deleteQuestions(ids);
    }

    /**
     * 根据试题id获取单题详情
     *
     * @param id 试题id
     * @return 响应结果
     */
    @GetMapping("/get/{id}")
    public R<QuestionVo> get(@PathVariable Long id) {
        return questionService.get(id);
    }
    /**
     * 根据题库id 获取题库下所有试题
     */
    @GetMapping("/list/{repoId}")
     public R<List<QuestionVo>> list(@PathVariable Long repoId) {
         return questionService.list(repoId);
    }
}
