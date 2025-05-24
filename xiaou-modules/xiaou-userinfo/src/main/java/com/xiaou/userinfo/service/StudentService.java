package com.xiaou.userinfo.service;

import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.userinfo.domain.bo.UStudentBO;
import com.xiaou.userinfo.domain.vo.StudentInfoVO;
import com.xiaou.userinfo.domain.vo.StudentVO;
import com.xiaou.userinfo.domain.vo.UClassVO;
import com.xiaou.userinfo.page.StudentPageReqDto;

public interface StudentService {
    R<StudentVO> addStudent(UStudentBO studentBO);


    R<StudentVO> updateStudent(UStudentBO studentBO);

    R<Void> deleteStudent(String studentNumber);



    R<PageRespDto<StudentInfoVO>> allStudentPage(StudentPageReqDto pageReqDto);
}
