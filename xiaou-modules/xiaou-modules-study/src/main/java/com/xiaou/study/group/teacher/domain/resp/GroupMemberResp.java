package com.xiaou.study.group.teacher.domain.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaou.study.group.teacher.domain.entity.GroupMember;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.util.Date;
@Data
@AutoMapper(target = GroupMember.class)
public class GroupMemberResp {


    /**
     * 用户ID（学生ID）
     */
    private String userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 加入时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date joinedAt;
}
