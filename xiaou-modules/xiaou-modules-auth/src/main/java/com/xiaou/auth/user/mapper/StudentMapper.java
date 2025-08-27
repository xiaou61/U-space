package com.xiaou.auth.user.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaou.auth.user.domain.entity.StudentEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StudentMapper extends BaseMapper<StudentEntity> {

    /**
     * 根据学生ID查询学生姓名
     * @param studentId 学生ID
     * @return 学生姓名
     */
    @Select("SELECT name FROM u_student WHERE id = #{studentId} AND status = 1")
    String selectNameById(String studentId);

    /**
     * 根据学生ID查询学生头像
     * @param studentId 学生ID
     * @return 学生头像URL
     */
    @Select("SELECT avatar FROM u_student WHERE id = #{studentId} AND status = 1")
    String selectAvatarById(String studentId);
}




