package com.xiaou.studentlife.controller;

import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.studentlife.domain.bo.ClassScheduleBo;
import com.xiaou.studentlife.domain.vo.ClassScheduleVo;
import com.xiaou.studentlife.service.ClassScheduleService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Validated
@RequestMapping("/class-schedule")
@RestController
public class ClassScheduleController {
    @Resource
    private ClassScheduleService classScheduleService;

    /**
     * 添加课程
     */
    @PostMapping("/add")
    public R<ClassScheduleVo> addClassSchedule(@RequestBody ClassScheduleBo scheduleBo) {
        return classScheduleService.addClassSchedule(scheduleBo);
    }

    @DeleteMapping("/delete/{id}")
    public R<String> deleteClassSchedule(@PathVariable Long id) {
        return classScheduleService.deleteClassSchedule(id);
    }

    @PutMapping("/update/{id}")
    public R<ClassScheduleVo> updateClassSchedule(@PathVariable Long id, @RequestBody ClassScheduleBo scheduleBo) {
        return classScheduleService.updateClassSchedule(id, scheduleBo);
    }

    /**
     * 根据班级id查询课表
     */
    @GetMapping("/list/{classId}")
    public R<List<ClassScheduleVo>> listClassSchedule(@PathVariable Long classId) {
        return classScheduleService.listClassSchedule(classId);
    }
    /**
     * 查询所有班级课表分页
     */
    @PostMapping("/list/page")
    public R<PageRespDto<ClassScheduleVo>> listClassSchedulePage(@RequestBody PageReqDto pageReqDto) {
        return classScheduleService.listClassSchedulePage(pageReqDto);
    }


}
