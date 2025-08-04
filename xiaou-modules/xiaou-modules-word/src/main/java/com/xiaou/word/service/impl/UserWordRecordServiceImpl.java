package com.xiaou.word.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.common.utils.QueryWrapperUtil;
import com.xiaou.satoken.utils.LoginHelper;
import com.xiaou.word.domain.entity.UserWordRecord;
import com.xiaou.word.domain.req.WordSelectReq;
import com.xiaou.word.domain.resp.UserWordRecordResp;
import com.xiaou.word.domain.resp.WordResp;
import com.xiaou.word.mapper.UserWordRecordMapper;
import com.xiaou.word.mapper.WordMapper;
import com.xiaou.word.service.UserWordRecordService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserWordRecordServiceImpl extends ServiceImpl<UserWordRecordMapper, UserWordRecord>
        implements UserWordRecordService {

    @Resource
    private LoginHelper loginHelper;
    @Resource
    private UserWordRecordMapper userWordRecordMapper;
    @Resource
    private WordMapper wordMapper;

    @Override
    public R<String> select(WordSelectReq req) {
        try {
            String currentAppUserId = loginHelper.getCurrentAppUserId();

            QueryWrapper<UserWordRecord> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", currentAppUserId)
                    .eq("word_id", req.getWordId());

            UserWordRecord existingRecord = userWordRecordMapper.selectOne(queryWrapper);
            Date now = new Date();

            if (existingRecord == null) {
                // 创建新记录
                UserWordRecord newRecord = new UserWordRecord();
                newRecord.setUserId(currentAppUserId);
                newRecord.setWordId(req.getWordId());
                newRecord.setKnowCount(req.getType() == 1 ? 1 : 0);
                newRecord.setNotKnowCount(req.getType() == 2 ? 1 : 0);
                newRecord.setLastStudyTime(now);
                newRecord.setCreatedAt(now);
                newRecord.setUpdatedAt(now);

                userWordRecordMapper.insert(newRecord);
            } else {
                // 更新现有记录
                if (req.getType() == 1) {
                    existingRecord.setKnowCount(existingRecord.getKnowCount() + 1);
                } else if (req.getType() == 2) {
                    existingRecord.setNotKnowCount(existingRecord.getNotKnowCount() + 1);
                }
                existingRecord.setLastStudyTime(now);
                existingRecord.setUpdatedAt(now);

                userWordRecordMapper.updateById(existingRecord);
            }

            return R.ok("学习记录保存成功");
        } catch (Exception e) {
            return R.fail("保存学习记录失败：" + e.getMessage());
        }
    }


}
