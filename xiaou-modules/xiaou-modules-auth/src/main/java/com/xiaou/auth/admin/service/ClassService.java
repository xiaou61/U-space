package com.xiaou.auth.admin.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.auth.admin.domain.entity.ClassEntity;
import com.xiaou.auth.admin.domain.excel.ClassEntityExcel;
import com.xiaou.auth.admin.domain.req.ClassReq;
import com.xiaou.auth.admin.domain.resp.ClassResp;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;

import java.util.List;

/**
* @author Lenovo
* @description 针对表【u_class(班级信息表)】的数据库操作Service
* @createDate 2025-06-29 12:26:44
*/
public interface ClassService extends IService<ClassEntity> {


    R<ClassResp> add(ClassReq req);

    R<String> delete(String id);

    R<ClassResp> updateClass(ClassReq req, String id);

    R<PageRespDto<ClassResp>> pageClass(PageReqDto req);

    void saveBatchFromExcel(List<ClassEntityExcel> dataList);

    List<ClassEntityExcel> getExcelData();
}
