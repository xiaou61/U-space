package com.xiaou.study.group.teacher.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.auth.user.mapper.StudentMapper;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.common.utils.QueryWrapperUtil;
import com.xiaou.satoken.utils.LoginHelper;
import com.xiaou.study.group.teacher.domain.entity.Signin;
import com.xiaou.study.group.teacher.domain.entity.SigninRecord;
import com.xiaou.study.group.teacher.domain.req.SigninReq;
import com.xiaou.study.group.teacher.domain.resp.SigninRecordResp;
import com.xiaou.study.group.teacher.domain.resp.SigninResp;
import com.xiaou.study.group.teacher.handler.SigninHandler;
import com.xiaou.study.group.teacher.mapper.GroupMapper;
import com.xiaou.study.group.teacher.mapper.SigninMapper;
import com.xiaou.study.group.teacher.mapper.SigninRecordMapper;
import com.xiaou.study.group.teacher.service.SigninService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SigninServiceImpl extends ServiceImpl<SigninMapper, Signin>
    implements SigninService {

    @Resource
    private List<SigninHandler> signinHandlers;
    @Resource
    private LoginHelper loginHelper;
    @Resource
    private GroupMapper groupMapper;
    @Resource
    private SigninRecordMapper signinRecordMapper;
    @Resource
    private StudentMapper studentMapper;

    @Override
    public R<String> add(SigninReq req) {
        //首先判断是什么签到
        for (SigninHandler handler : signinHandlers) {
            if (handler.support(req.getType())) {
                handler.handle(req);
                return R.ok("发布签到成功");
            }
        }
        return R.fail("不支持的签到类型");
    }

    @Override
    public R<PageRespDto<SigninResp>> PageList(PageReqDto req) {
        IPage<Signin> iPage= new Page<>(req.getPageNum(), req.getPageSize());
        QueryWrapper<Signin> queryWrapper = new QueryWrapper<>();
        //仅自己
        queryWrapper.eq("creator_id",loginHelper.getCurrentAppUserId());
        IPage<Signin> signinIPage = baseMapper.selectPage(iPage, queryWrapper);
        List<SigninResp> signinResp = MapstructUtils.convert(signinIPage.getRecords(), SigninResp.class);
        //添加群组名称
        signinResp.forEach(item -> item.setGroupName(groupMapper.selectById(item.getGroupId()).getName()));
        return R.ok(PageRespDto.of(req.getPageNum(),req.getPageSize(),signinIPage.getTotal(),signinResp));
    }

    @Override
    public R<List<SigninRecordResp>> detail(String id) {
        //根据id查找signinid
        QueryWrapper<SigninRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("signin_id",id);
        List<SigninRecord> signinRecords = signinRecordMapper.selectList(queryWrapper);
        List<SigninRecordResp> convert = MapstructUtils.convert(signinRecords, SigninRecordResp.class);
        //添加用户名称
        convert.forEach(item -> item.setStudentName(studentMapper.selectById(item.getStudentId()).getName()));
        return R.ok(convert);
    }

}




