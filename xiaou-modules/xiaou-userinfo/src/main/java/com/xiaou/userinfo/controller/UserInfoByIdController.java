package com.xiaou.userinfo.controller;

import com.xiaou.common.domain.R;
import com.xiaou.userinfo.domain.entity.ClassEntity;
import com.xiaou.userinfo.domain.entity.Major;
import com.xiaou.userinfo.domain.vo.UClassVO;
import com.xiaou.userinfo.domain.vo.UCollegeVO;
import com.xiaou.userinfo.domain.vo.UMajorVO;
import com.xiaou.userinfo.service.ClassService;
import com.xiaou.userinfo.service.CollegeService;
import com.xiaou.userinfo.service.MajorService;
import com.xiaou.userinfo.service.UserInfoByIdService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 根据id查询对应信息
 */
@RestController
@RequestMapping("/info")
public class UserInfoByIdController {

    @Resource
    private UserInfoByIdService userInfoByIdService;
    /**
     * 根据学院id(college_id)查询学院下面包括的专业 查询major表
     */
    @GetMapping("/major/{id}")
    public R<List<Major>> getCollegeById(@PathVariable Long id) {
        return userInfoByIdService.getMajor(id);
    }

    /**
     * 根据专业id(major_id)查询专业下面包括的班级 查询class表
     */
    @GetMapping("/class/{id}")
    public R<List<ClassEntity>> getClassById(@PathVariable Long id) {
        return userInfoByIdService.getClazz(id);
    }

    /**
     * 根据学号查询班级id 查询u-student-info-link
     */
    @GetMapping("/classId/{id}")
    public R<Long> getClassIdById(@PathVariable Long id) {
        return userInfoByIdService.getClassIdByStudentNumber(id);
    }
}
