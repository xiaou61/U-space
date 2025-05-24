package com.xiaou.log.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.log.domain.bo.SysOperLogBo;
import com.xiaou.log.domain.entity.SysOperLog;
import com.xiaou.log.domain.vo.SysOperLogVo;
import com.xiaou.log.event.OperLogEvent;
import com.xiaou.log.mapper.SysOperLogMapper;
import com.xiaou.log.service.ISysOperLogService;
import jakarta.annotation.Resource;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@NoArgsConstructor
public class ISysOperLogServiceImpl implements ISysOperLogService {

    @Resource
    private SysOperLogMapper baseMapper;

    /**
     * 操作日志记录
     *
     * @param operLogEvent 操作日志事件
     */
    @Async
    @EventListener
    public void recordOper(OperLogEvent operLogEvent) {
        SysOperLogBo operLog = new SysOperLogBo();
        BeanUtils.copyProperties(operLogEvent, operLog);
        // 远程查询操作地点
        operLog.setOperLocation("河北省秦皇岛市");
        insertOperlog(operLog);
    }

    @Override
    public R<PageRespDto<SysOperLogVo>> selectPageOperLogList(PageReqDto dto) {
        IPage<SysOperLog> page = new Page<>();
        page.setCurrent(dto.getPageNum());
        page.setSize(dto.getPageSize());

        IPage<SysOperLog> collegeIPage = baseMapper.selectPage(page, new QueryWrapper<>());

        // 转换实体为 VO
        List<SysOperLogVo> voList = collegeIPage.getRecords().stream()
                .map(record -> BeanUtil.copyProperties(record, SysOperLogVo.class))
                .toList();

        return R.ok(PageRespDto.of(dto.getPageNum(), dto.getPageSize(), page.getTotal(), voList));
    }


    /**
     * 新增操作日志
     *
     * @param bo 操作日志对象
     */
    @Override
    public void insertOperlog(SysOperLogBo bo) {
        SysOperLog operLog = new SysOperLog();
        BeanUtils.copyProperties(bo, operLog);
        operLog.setOperTime(new Date());
        baseMapper.insert(operLog);
    }

}
