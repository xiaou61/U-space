package com.xiaou.auth.user.domain.resp;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.xiaou.auth.admin.domain.entity.ClassEntity;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

@Data
@AutoMapper(target = ClassEntity.class)
public class StudentLoginClassResp {

    /**
     * 主键ID
     */

    private String id;

    /**
     * 班级名称
     */
    private String className;

}
