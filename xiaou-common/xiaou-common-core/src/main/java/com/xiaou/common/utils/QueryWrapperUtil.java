package com.xiaou.common.utils;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiaou.common.page.PageReqDto;

import java.util.List;

public class QueryWrapperUtil {

    /**
     * 给 QueryWrapper 添加排序（布尔控制升降序）
     */
    public static <T> void applySorting(QueryWrapper<T> wrapper, PageReqDto dto, List<String> validFields) {
        String field = dto.getSortField();
        Boolean desc = dto.getDesc();

        if (StrUtil.isNotBlank(field) && validFields.contains(field)) {
            wrapper.orderBy(true, desc == null || !desc, field); // true：是否添加排序，false：升序
        }
    }
}
