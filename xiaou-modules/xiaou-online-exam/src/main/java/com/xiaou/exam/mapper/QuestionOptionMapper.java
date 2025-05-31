package com.xiaou.exam.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaou.exam.domain.entity.QuestionOption;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface QuestionOptionMapper extends BaseMapper<QuestionOption> {

    void insertBatch(List<QuestionOption> options);
}




