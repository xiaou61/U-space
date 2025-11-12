package com.xiaou.resume.mapper;

import com.xiaou.resume.domain.ResumeTemplate;
import com.xiaou.resume.dto.TemplateQueryRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模板Mapper
 */
@Mapper
public interface ResumeTemplateMapper {

    int insert(ResumeTemplate template);

    int updateById(ResumeTemplate template);

    int deleteById(Long id);

    ResumeTemplate selectById(Long id);

    List<ResumeTemplate> selectList(@Param("request") TemplateQueryRequest request);

    long countAll();
}
