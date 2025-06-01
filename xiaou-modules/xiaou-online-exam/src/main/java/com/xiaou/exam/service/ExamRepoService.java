package com.xiaou.exam.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.common.domain.R;
import com.xiaou.exam.domain.bo.ExamRepoBo;
import com.xiaou.exam.domain.entity.ExamRepo;
import com.xiaou.exam.domain.vo.ExamRepoVo;
import org.xnio.Result;

import java.util.List;


public interface ExamRepoService extends IService<ExamRepo> {

    R<String> addRepo(ExamRepoBo examRepoBo);

    R<String> updateRepo(Long id,ExamRepoBo examRepoBo);

    R<String> deleteRepo(Long id);

    R<List<ExamRepoVo>> getRepoByCategoryId(Long categoryId);
}
