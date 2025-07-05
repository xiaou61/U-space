package com.xiaou.study.group.teacher.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.common.domain.R;
import com.xiaou.study.group.student.domain.resp.GroupResp;
import com.xiaou.study.group.teacher.domain.entity.GroupMember;

import java.util.List;

public interface GroupMemberService extends IService<GroupMember> {


    R<String> join(String id);


    R<List<GroupResp>> listByMy();
}
