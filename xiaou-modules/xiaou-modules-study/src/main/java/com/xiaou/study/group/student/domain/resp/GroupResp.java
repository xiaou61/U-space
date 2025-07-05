package com.xiaou.study.group.student.domain.resp;

import com.xiaou.study.group.teacher.domain.entity.Group;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

@Data
@AutoMapper(target = Group.class)
public class GroupResp {

    private String id;
    /**
     * 群组名称
     */
    private String name;
    /**
     * 群组描述
     */
    private String description;

}
