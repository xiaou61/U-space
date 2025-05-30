package com.xiaou.exam.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.common.domain.R;
import com.xiaou.exam.domain.bo.QuestionCategoryBo;
import com.xiaou.exam.domain.entity.QuestionCategory;
import com.xiaou.exam.domain.vo.QuestionCategoryVo;
import org.xnio.Result;

import java.util.List;


public interface QuestionCategoryService extends IService<QuestionCategory> {

    R<String> addCategory(QuestionCategoryBo category);

    R<String> updateCategory(QuestionCategoryBo category,Long id);

    R<String> deleteCategory(Long id);

    R<List<QuestionCategoryVo>> getCategoryTree();

    R<List<QuestionCategoryVo>> getFirstLevelCategories();

    R<List<QuestionCategoryVo>> getChildrenCategories(Long id);
}
