package com.xiaou.bbs.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PostCategoryEnum {
    
    SECOND_HAND("second_hand", "二手闲置"),
    HELP("help", "打听求助"),
    DATING("dating", "恋爱交友"),
    CAMPUS("campus", "校园趣事"),
    JOB("job", "兼职招聘"),
    OTHER("other", "其他");

    @EnumValue // MyBatis-Plus 会使用这个值进行存储
    private final String code;
    private final String label;

}
