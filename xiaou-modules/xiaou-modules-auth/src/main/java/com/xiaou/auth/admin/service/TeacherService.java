package com.xiaou.auth.admin.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.auth.admin.domain.entity.Teacher;
import com.xiaou.auth.admin.domain.req.TeacherLoginReq;
import com.xiaou.auth.admin.domain.req.TeacherReq;
import com.xiaou.auth.admin.domain.resp.TeacherResp;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;

public interface TeacherService extends IService<Teacher> {


    R<String> add(TeacherReq req);

    R<String> delete(String id);

    R<PageRespDto<TeacherResp>> pageTeacher(PageReqDto req);
}
