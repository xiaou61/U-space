package com.xiaou.study.group.teacher.domain.req;

import com.alibaba.fastjson2.JSON;
import com.xiaou.study.group.teacher.domain.entity.HomeworkSubmission;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.util.List;

@Data
@AutoMapper(target = HomeworkSubmission.class)
public class HomeworkSubmissionReq {

    /**
     * 学生提交的文本内容
     */
    private String content;

    /**
     * 附件地址的JSON数组，存储多个附件链接
     */
    private List<String> attachmentUrls;

}
