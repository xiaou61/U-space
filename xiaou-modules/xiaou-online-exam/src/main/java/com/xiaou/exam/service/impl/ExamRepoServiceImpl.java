package com.xiaou.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.xiaou.common.domain.R;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.exam.domain.bo.ExamRepoBo;
import com.xiaou.exam.domain.entity.ExamRepo;
import com.xiaou.exam.domain.entity.QuestionCategory;
import com.xiaou.exam.domain.vo.ExamRepoVo;
import com.xiaou.exam.mapper.ExamRepoMapper;
import com.xiaou.exam.service.ExamRepoService;
import com.xiaou.exam.service.QuestionCategoryService;
import com.xiaou.utils.LoginHelper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.xnio.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class ExamRepoServiceImpl extends ServiceImpl<ExamRepoMapper, ExamRepo>
        implements ExamRepoService {

    @Resource
    private QuestionCategoryService categoryService;

    @Override
    public R<String> addRepo(ExamRepoBo examRepoBo) {
        // 检查分类ID是否存在
        if (examRepoBo.getCategoryId() != null) {
            QuestionCategory category = categoryService.getById(examRepoBo.getCategoryId());
            if (category == null) {
                return R.fail("分类不存在");
            }
        }
        ExamRepo examRepo = MapstructUtils.convert(examRepoBo, ExamRepo.class);
        examRepo.setUserId(LoginHelper.getCurrentAppUserId());
        if (save(examRepo)) {
            return R.ok("添加成功");
        }
        return R.fail("添加失败");
    }

    @Override
    public R<String> updateRepo(Long id, ExamRepoBo examRepoBo) {
        // 检查分类ID是否存在
        if (id != null) {
            QuestionCategory category = categoryService.getById(id);
            if (category == null) {
                return R.fail("分类不存在");
            }
        }
        //根据id 更新
        ExamRepo examRepo = MapstructUtils.convert(examRepoBo, ExamRepo.class);
        examRepo.setId(id);
        if (updateById(examRepo)) {
            return R.ok("更新成功");
        }
        return R.fail("更新失败");
    }

    @Override
    public R<String> deleteRepo(Long id) {
        // todo 后面如果有试题的话，要先清除试题跟这个分类的关联
        if (removeById(id)) {
            return R.ok("删除成功");
        }

        return R.fail("删除失败");
    }

    @Override
    public R<ExamRepoVo> getRepoByCategoryId(Long categoryId) {
        if (categoryId != null) {
            LambdaQueryWrapper<ExamRepo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ExamRepo::getCategoryId, categoryId);
            ExamRepo examRepo = getOne(queryWrapper);
            if (examRepo != null) {
                ExamRepoVo examRepoVo = MapstructUtils.convert(examRepo, ExamRepoVo.class);
                examRepoVo.setCategoryId(categoryId);
                examRepoVo.setCategoryName(categoryService.getById(categoryId).getName());
                //todo设置题目数量
                return R.ok(examRepoVo);
            }
        }
        return R.fail("分类Id不存在");
    }

}




