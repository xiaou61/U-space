package com.xiaou.exam.domain.bo;

import com.xiaou.exam.domain.entity.PracticeSession;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data

public class PracticeStartBo {

    @NotNull(message = "题库ID不能为空")
    private Long repoId;

    /**
     * 练习题数（可选：比如前端控制）
     */
    private Integer questionCount;
}
