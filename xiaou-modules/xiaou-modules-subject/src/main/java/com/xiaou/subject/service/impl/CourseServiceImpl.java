package com.xiaou.subject.service.impl;


import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.subject.domain.entity.ClassCourse;
import com.xiaou.subject.domain.entity.Course;
import com.xiaou.subject.domain.req.CourseReq;
import com.xiaou.subject.domain.resp.CourseResp;
import com.xiaou.subject.mapper.ClassCourseMapper;
import com.xiaou.subject.mapper.CourseMapper;
import com.xiaou.subject.service.CourseService;
import jakarta.annotation.Resource;
import org.springframework.amqp.support.converter.ClassMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course>
        implements CourseService {

    @Resource
    private ClassCourseMapper classCourseMapper;

    @Override
    public R<String> create(CourseReq courseReq) {
        Course convert = MapstructUtils.convert(courseReq, Course.class);
        baseMapper.insert(convert);
        return R.ok("添加成功");
    }

    @Override
    public R<String> updateSubject(String id, CourseReq courseReq) {
        Course convert = MapstructUtils.convert(courseReq, Course.class);
        baseMapper.updateById(convert);
        return R.ok("修改成功");
    }

    @Override
    public R<String> deleteSubject(String id) {
        baseMapper.deleteById(id);
        return R.ok("删除成功");
    }

    @Override
    public R<PageRespDto<CourseResp>> listSubject(PageReqDto dto) {
        IPage<Course> iPage = new Page<>(dto.getPageNum(), dto.getPageSize());
        QueryWrapper<Course> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        IPage<Course> page = baseMapper.selectPage(iPage, queryWrapper);
        return R.ok(PageRespDto.of(page.getCurrent(),
                page.getSize(),
                page.getTotal(),
                MapstructUtils.convert(page.getRecords(), CourseResp.class)));
    }

    @Override
    public R<String> addClassCourse(String courseId, String classId) {
        ClassCourse classCourse = new ClassCourse();
        classCourse.setClassId(classId);
        classCourse.setCourseId(courseId);
        classCourseMapper.insert(classCourse);
        return R.ok("添加成功");
    }

    @Override
    public R<List<String>> listClass(String courseId) {
        QueryWrapper<ClassCourse> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id", courseId);
        List<ClassCourse> classCourses = classCourseMapper.selectList(queryWrapper);
        //根据班级id 批量查询班级姓名
        List<String> classIds = classCourses.stream().map(ClassCourse::getClassId).toList();
        return R.ok(classIds);
    }
}




