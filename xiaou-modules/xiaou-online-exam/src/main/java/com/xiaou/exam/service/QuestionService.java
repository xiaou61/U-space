package com.xiaou.exam.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.common.domain.R;
import com.xiaou.exam.domain.bo.QuestionBo;
import com.xiaou.exam.domain.entity.Question;
import com.xiaou.exam.domain.vo.QuestionVo;

import java.util.List;


public interface QuestionService extends IService<Question> {

    R<String> addQuestion(QuestionBo questionBo);

    R<String> deleteQuestions(String ids);

    R<QuestionVo> get(Long id);

    R<List<QuestionVo>> list(Long repoId);

    R<List<Long>> ids(Long repoId);
}
