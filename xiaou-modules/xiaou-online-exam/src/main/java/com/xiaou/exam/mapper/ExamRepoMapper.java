package com.xiaou.exam.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaou.exam.domain.entity.ExamRepo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ExamRepoMapper extends BaseMapper<ExamRepo> {

    @Update("UPDATE u_exam_repo SET question_count = question_count + 1 WHERE id = #{repo_id}")
    void incrementQuestionCount(@Param("repo_id") Long repoId);

    @Update("UPDATE u_exam_repo SET question_count = question_count - #{count} WHERE id = #{repoId} AND question_count >= #{count}")
    void decrementQuestionCount(@Param("repoId") Long repoId, @Param("count") int count);

}




