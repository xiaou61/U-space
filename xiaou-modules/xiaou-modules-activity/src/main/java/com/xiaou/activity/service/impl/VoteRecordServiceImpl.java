package com.xiaou.activity.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.activity.domain.entity.VoteRecord;
import com.xiaou.activity.mapper.VoteRecordMapper;
import com.xiaou.activity.service.VoteRecordService;
import org.springframework.stereotype.Service;

@Service
public class VoteRecordServiceImpl extends ServiceImpl<VoteRecordMapper, VoteRecord>
    implements VoteRecordService {

}




