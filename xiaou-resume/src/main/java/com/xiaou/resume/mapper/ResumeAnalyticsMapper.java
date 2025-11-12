package com.xiaou.resume.mapper;

import com.xiaou.resume.domain.ResumeAnalytics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * 统计Mapper
 */
@Mapper
public interface ResumeAnalyticsMapper {

    int insert(ResumeAnalytics analytics);

    ResumeAnalytics selectByResumeId(Long resumeId);

    int increaseViewCount(Long resumeId);

    int increaseExportCount(Long resumeId);

    int increaseShareCount(Long resumeId);

    int updateLastAccessTime(@Param("resumeId") Long resumeId,
                             @Param("lastAccessTime") LocalDateTime lastAccessTime);

    int deleteByResumeId(Long resumeId);

    ResumeAnalytics aggregateAll();
}
