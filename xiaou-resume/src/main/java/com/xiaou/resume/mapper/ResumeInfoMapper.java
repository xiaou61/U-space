package com.xiaou.resume.mapper;

import com.xiaou.resume.domain.ResumeInfo;
import com.xiaou.resume.dto.ResumeListQueryRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 简历信息Mapper
 */
@Mapper
public interface ResumeInfoMapper {

    int insert(ResumeInfo info);

    int updateById(ResumeInfo info);

    int deleteById(Long id);

    ResumeInfo selectById(Long id);

    ResumeInfo selectByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    List<ResumeInfo> selectByUserId(@Param("userId") Long userId,
                                    @Param("request") ResumeListQueryRequest request);

    long countAll();

    long countByStatus(@Param("status") Integer status);

    List<ResumeInfo> selectResumesWithoutSections();
}
