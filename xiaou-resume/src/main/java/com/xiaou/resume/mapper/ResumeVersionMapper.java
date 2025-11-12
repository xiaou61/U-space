package com.xiaou.resume.mapper;

import com.xiaou.resume.domain.ResumeVersion;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 版本Mapper
 */
@Mapper
public interface ResumeVersionMapper {

    int insert(ResumeVersion version);

    List<ResumeVersion> selectByResumeId(Long resumeId);

    int deleteByResumeId(Long resumeId);
}
