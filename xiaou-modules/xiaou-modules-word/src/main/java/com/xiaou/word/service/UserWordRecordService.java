package com.xiaou.word.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.common.domain.R;
import com.xiaou.word.domain.entity.UserWordRecord;
import com.xiaou.word.domain.req.WordSelectReq;

public interface UserWordRecordService extends IService<UserWordRecord> {

    R<String> select(WordSelectReq req);

}
