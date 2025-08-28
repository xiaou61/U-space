package com.xiaou.bbs.domain.resp;

import lombok.Data;

@Data
public class BbsCategoryResp {

    private String id;

    /**
     * 分类名称，如 学习、生活、娱乐等
     */
    private String name;

    /**
     * 分类状态，1启用 0禁用
     */
    private Integer status;
}
