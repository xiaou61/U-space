package com.xiaou.userinfo.controller;

import com.xiaou.common.domain.R;
import com.xiaou.userinfo.domain.bo.UCollegeBO;
import com.xiaou.userinfo.domain.vo.UCollegeVO;
import com.xiaou.userinfo.service.CollegeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/college")
public class CollegeController {

    @Resource
    private CollegeService collegeService;

    /**
     * 添加学院
     */
    @PostMapping("/add")
    public R<UCollegeVO> addCollege(@RequestBody UCollegeBO collegeBO) {

        return null;
    }
}
