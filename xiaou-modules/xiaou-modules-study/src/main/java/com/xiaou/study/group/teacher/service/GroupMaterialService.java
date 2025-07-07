package com.xiaou.study.group.teacher.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.study.group.teacher.domain.entity.GroupMaterial;
import com.xiaou.study.group.teacher.domain.req.GroupMaterialReq;
import com.xiaou.study.group.teacher.domain.resp.GroupMaterialResp;

import java.util.List;

public interface GroupMaterialService extends IService<GroupMaterial> {

    R<String> add(GroupMaterialReq req);

    R<String> delete(String id);

    R<PageRespDto<GroupMaterialResp>> listPage(PageReqDto req);

    R<List<GroupMaterialResp>> listByMy(String groupId);
}
