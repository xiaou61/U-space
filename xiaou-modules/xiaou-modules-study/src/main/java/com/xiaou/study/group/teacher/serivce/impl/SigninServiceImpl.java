package com.xiaou.study.group.teacher.serivce.impl;


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
import com.xiaou.study.group.teacher.domain.entity.Signin;
import com.xiaou.study.group.teacher.domain.req.SigninReq;
import com.xiaou.study.group.teacher.domain.resp.SigninResp;
import com.xiaou.study.group.teacher.handler.SigninHandler;
import com.xiaou.study.group.teacher.mapper.SigninMapper;
import com.xiaou.study.group.teacher.serivce.SigninService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SigninServiceImpl extends ServiceImpl<SigninMapper, Signin>
    implements SigninService {

    @Resource
    private List<SigninHandler> signinHandlers;
    @Autowired
    private LoginHelper loginHelper;

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
        QueryWrapperUtil.applySorting(queryWrapper,req, List.of(req.getSortField()));
        //仅自己
        queryWrapper.eq("creator_id",loginHelper.getCurrentAppUserId());
        IPage<Signin> signinIPage = baseMapper.selectPage(iPage, queryWrapper);
        List<SigninResp> signinResp = MapstructUtils.convert(signinIPage.getRecords(), SigninResp.class);
        return R.ok(PageRespDto.of(req.getPageNum(),req.getPageSize(),signinIPage.getTotal(),signinResp));
    }

}




