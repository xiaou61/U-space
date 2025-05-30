package com.xiaou.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.xiaou.common.domain.R;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.exam.domain.bo.QuestionCategoryBo;
import com.xiaou.exam.domain.entity.QuestionCategory;
import com.xiaou.exam.domain.vo.QuestionCategoryVo;
import com.xiaou.exam.mapper.QuestionCategoryMapper;
import com.xiaou.exam.service.QuestionCategoryService;
import org.springframework.stereotype.Service;
import org.xnio.Result;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class QuestionCategoryServiceImpl extends ServiceImpl<QuestionCategoryMapper, QuestionCategory>
        implements QuestionCategoryService {

    @Override
    public R<String> addCategory(QuestionCategoryBo category) {
        QuestionCategory questionCategory = MapstructUtils.convert(category, QuestionCategory.class);
        questionCategory.setCreateTime(new Date());
        baseMapper.insert(questionCategory);
        return R.ok("添加成功");
    }

    @Override
    public R<String> updateCategory(QuestionCategoryBo category, Long id) {
        QuestionCategory questionCategory = MapstructUtils.convert(category, QuestionCategory.class);
        questionCategory.setId(id);
        baseMapper.updateById(questionCategory);
        return R.ok("修改成功");
    }

    @Override
    public R<String> deleteCategory(Long id) {
        //检查是否有子分类
        QueryWrapper<QuestionCategory> qw = new QueryWrapper<>();
        qw.eq("parent_id", id);
        if (baseMapper.selectCount(qw) > 0) {
            return R.fail("该分类下有子分类，请先删除子分类");
        }
        if (baseMapper.deleteById(id) > 0) {
            return R.ok("删除成功");
        }
        return R.fail("删除失败");
    }

    //todo 发文
    @Override
    public R<List<QuestionCategoryVo>> getCategoryTree() {
        // 1. 获取所有分类
        List<QuestionCategory> allCategories = baseMapper.selectList(null);
        // 2. 实体类转 VO
        List<QuestionCategoryVo> voList = MapstructUtils.convert(allCategories, QuestionCategoryVo.class);
        // 3. 构建树形结构
        List<QuestionCategoryVo> tree = buildCategoryTree(voList, 0L);
        return R.ok(tree);
    }

    @Override
    public R<List<QuestionCategoryVo>> getFirstLevelCategories() {
        LambdaQueryWrapper<QuestionCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionCategory::getParentId, 0);

        List<QuestionCategory> categories = list(wrapper);
        List<QuestionCategoryVo> vo = MapstructUtils.convert(categories, QuestionCategoryVo.class);

        return R.ok(vo);
    }

    @Override
    public R<List<QuestionCategoryVo>> getChildrenCategories(Long parentId) {
        LambdaQueryWrapper<QuestionCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionCategory::getParentId, parentId);
        List<QuestionCategory> categories = list(wrapper);
        if (categories.size() > 0) {
            List<QuestionCategoryVo> vo = MapstructUtils.convert(categories, QuestionCategoryVo.class);
            return R.ok(vo);
        }
        return R.fail("当前分类没有子分类");
    }

    private List<QuestionCategoryVo> buildCategoryTree(List<QuestionCategoryVo> all, Long parentId) {
        return all.stream()
                .filter(cat -> cat.getParentId().equals(parentId))
                .peek(cat -> {
                    List<QuestionCategoryVo> children = buildCategoryTree(all, cat.getId());
                    cat.setChildren(children);
                })
                .collect(Collectors.toList());
    }


}




