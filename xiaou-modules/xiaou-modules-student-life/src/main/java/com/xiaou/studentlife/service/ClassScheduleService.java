package com.xiaou.studentlife.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.studentlife.domain.bo.ClassScheduleBo;
import com.xiaou.studentlife.domain.entity.ClassSchedule;
import com.xiaou.studentlife.domain.vo.ClassScheduleVo;

import java.util.List;


public interface ClassScheduleService extends IService<ClassSchedule> {

    R<ClassScheduleVo> addClassSchedule(ClassScheduleBo scheduleBo);

    R<String> deleteClassSchedule(Long id);

    R<ClassScheduleVo> updateClassSchedule(Long id,ClassScheduleBo scheduleBo);

    R<List<ClassScheduleVo>> listClassSchedule(Long userid);

    R<PageRespDto<ClassScheduleVo>> listClassSchedulePage(PageReqDto pageReqDto);
}
