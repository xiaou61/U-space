package com.xiaou.userinfo.controller;

import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.userinfo.domain.bo.UStudentBO;
import com.xiaou.userinfo.domain.vo.StudentInfoVO;
import com.xiaou.userinfo.domain.vo.StudentVO;
import com.xiaou.userinfo.domain.vo.UClassVO;
import com.xiaou.userinfo.domain.vo.UMajorVO;
import com.xiaou.userinfo.page.StudentPageReqDto;
import com.xiaou.userinfo.service.StudentService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/student")
@Validated
public class StudentController {
    @Resource
    private StudentService studentService;

    /**
     * 添加学生
     *
     * @param studentBO
     * @return
     */
    @PostMapping("/add")
    public R<StudentVO> addStudent(@RequestBody @Valid UStudentBO studentBO) {
        return studentService.addStudent(studentBO);
    }

    /**
     * 更新学生信息
     * @param studentBO
     * @return
     */
    @PostMapping("/update")
    public R<StudentVO> updateStudent(@RequestBody @Valid UStudentBO studentBO) {
        return studentService.updateStudent(studentBO);
    }

    /**
     * 删除学生信息
     * @param studentNumber
     * @return
     */
    @DeleteMapping("/delete/{studentNumber}")
    public R<Void> deleteStudent(@PathVariable String studentNumber) {
        return studentService.deleteStudent(studentNumber);
    }


    /**
     * 分页查询专业
     */
    @PostMapping("/page")
    public R<PageRespDto<StudentInfoVO>> allStudentPage(@RequestBody StudentPageReqDto pageReqDto) {
        return studentService.allStudentPage(pageReqDto);
    }

}
