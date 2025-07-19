package com.xiaou.room.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.common.domain.R;
import com.xiaou.room.domain.entity.DormRegister;
import com.xiaou.room.domain.req.DormRegisterReq;

public interface DormRegisterService extends IService<DormRegister> {


    R<Boolean> isNeedInputInfo();

    R<String> inputInfo(DormRegisterReq req);
}
