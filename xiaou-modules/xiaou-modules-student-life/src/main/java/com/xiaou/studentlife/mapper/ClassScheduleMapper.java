package com.xiaou.studentlife.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaou.studentlife.domain.entity.ClassSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ClassScheduleMapper extends BaseMapper<ClassSchedule> {

    /**
     * 根据 userId 查 student_number，再查 u_student_info_link 拿到 class_id
     */
    @Select("""
        SELECT info.class_id
          FROM u_student_user u
          JOIN u_student_info_link info
            ON u.student_number = info.student_number
         WHERE u.id = #{userId}
        """)
    Long selectClassIdByUserId(@Param("userId") Long userId);

}




