package com.xiaou.system.log.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.common.utils.QueryWrapperUtil;

import com.xiaou.log.event.OperLogEvent;
import com.xiaou.mq.utils.RabbitMQUtils;
import com.xiaou.system.log.domain.bo.SysOperLogBo;
import com.xiaou.system.log.domain.entity.SysOperLog;
import com.xiaou.system.log.domain.excel.SysOperLogExcelEntity;
import com.xiaou.system.log.domain.vo.SysOperLogVo;
import com.xiaou.system.log.mapper.SysOperLogMapper;
import com.xiaou.system.log.service.ISysOperLogService;
import jakarta.annotation.Resource;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
@NoArgsConstructor
public class ISysOperLogServiceImpl implements ISysOperLogService {

    private static final Logger log = LoggerFactory.getLogger(ISysOperLogServiceImpl.class);
    @Resource
    private SysOperLogMapper baseMapper;
    @Autowired
    private RabbitMQUtils rabbitMQUtils;

    /**
     * 操作日志记录
     *
     * @param operLogEvent 操作日志事件
     */
    @Async
    @EventListener
    public void recordOper(OperLogEvent operLogEvent) {
        SysOperLogBo convert = MapstructUtils.convert(operLogEvent, SysOperLogBo.class);
        // 远程查询操作地点
        //todo 代做
        convert.setOperLocation("河北省秦皇岛市");
        //发布MQ
        rabbitMQUtils.sendLogMessage(convert);
    }

    @Override
    public R<PageRespDto<SysOperLogVo>> selectPageOperLogList(PageReqDto dto) {
        IPage<SysOperLog> page = new Page<>();
        page.setCurrent(dto.getPageNum());
        page.setSize(dto.getPageSize());

        // 添加排序字段（以 create_time 字段为例）
        QueryWrapper<SysOperLog> queryWrapper = new QueryWrapper<>();
        QueryWrapperUtil.applySorting(queryWrapper, dto, List.of(dto.getSortField()));

        IPage<SysOperLog> collegeIPage = baseMapper.selectPage(page, queryWrapper);

        // 转换实体为 VO
        List<SysOperLogVo> voList = collegeIPage.getRecords().stream()
                .map(record -> BeanUtil.copyProperties(record, SysOperLogVo.class))
                .toList();

        return R.ok(PageRespDto.of(dto.getPageNum(), dto.getPageSize(), page.getTotal(), voList));
    }


    /**
     * 新增操作日志
     *废弃
     * @param bo 操作日志对象
     */
    @Override
    public void insertOperlog(SysOperLogBo bo) {
        return;
    }

    @Override
    public List<SysOperLogExcelEntity> getExcelData(LocalDateTime beginTime, LocalDateTime endTime) {
        LambdaQueryWrapper<SysOperLog> queryWrapper = Wrappers.lambdaQuery();

        // 添加时间范围条件
        queryWrapper
                .ge(beginTime != null, SysOperLog::getOperTime, beginTime)
                .le(endTime != null, SysOperLog::getOperTime, endTime);

        List<SysOperLog> entityList = baseMapper.selectList(queryWrapper);
        return MapstructUtils.convert(entityList, SysOperLogExcelEntity.class);
    }


}
