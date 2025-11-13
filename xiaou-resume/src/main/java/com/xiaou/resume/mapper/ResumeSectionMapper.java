package com.xiaou.resume.mapper;

import com.xiaou.resume.domain.ResumeSection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 简历模块Mapper
 */
@Mapper
public interface ResumeSectionMapper {

    int insertBatch(@Param("sections") List<ResumeSection> sections);

    int deleteByResumeId(Long resumeId);

    List<ResumeSection> selectByResumeId(Long resumeId);
}
