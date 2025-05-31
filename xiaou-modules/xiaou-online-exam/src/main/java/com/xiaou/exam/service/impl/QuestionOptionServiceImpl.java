package com.xiaou.exam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.xiaou.exam.domain.entity.QuestionOption;
import com.xiaou.exam.mapper.QuestionOptionMapper;
import com.xiaou.exam.service.QuestionOptionService;
import org.springframework.stereotype.Service;


@Service
public class QuestionOptionServiceImpl extends ServiceImpl<QuestionOptionMapper, QuestionOption>
    implements QuestionOptionService {

}




