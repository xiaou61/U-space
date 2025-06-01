package com.xiaou.hot.datasource;


import com.xiaou.hot.model.po.HotPost;

/**
 * 数据源接口（新接入数据源必须实现 ）
 *
 */
public interface DataSource {

    /**
     * 获取热榜数据
     *
     * @return {@link HotPost }
     */
    HotPost getHotPost();
}