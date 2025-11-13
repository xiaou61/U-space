package com.xiaou.resume.mapper;

import com.xiaou.resume.domain.ResumeShare;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分享Mapper
 */
@Mapper
public interface ResumeShareMapper {

    int insert(ResumeShare share);

    int updateById(ResumeShare share);

    ResumeShare selectActiveShare(@Param("resumeId") Long resumeId,
                                  @Param("now") LocalDateTime now);

    List<ResumeShare> selectExpiredShares(@Param("now") LocalDateTime now);

    int deleteByResumeId(Long resumeId);
}
