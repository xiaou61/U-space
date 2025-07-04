package com.xiaou.study.group.teacher.serivce;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.common.domain.R;
import com.xiaou.study.group.teacher.domain.entity.Group;
import com.xiaou.study.group.teacher.domain.req.GroupReq;

import java.util.List;

public interface GroupService extends IService<Group> {

    R<String> add(GroupReq req);

    R<String> delete(String id);

    R<List<Group>> listById();

    R<String> generateId(String groupId);

}
