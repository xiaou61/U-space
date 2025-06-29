package com.xiaou.common.utils;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiaou.common.page.PageReqDto;

import java.util.List;

public class QueryWrapperUtil {

    /**
     * 给 QueryWrapper 添加排序（布尔控制升降序）
     * 若字段为空，则默认使用 "created_at"
     */
    public static <T> void applySorting(QueryWrapper<T> wrapper, PageReqDto dto, List<String> validFields) {
        String field = dto.getSortField();
        Boolean desc = dto.getDesc();

        // 如果字段为空或非法，则使用默认字段 "created_at"
        if (StrUtil.isBlank(field) || !validFields.contains(field)) {
            field = "created_at";
        }

        wrapper.orderBy(true, desc == null || !desc, field);
    }

}
