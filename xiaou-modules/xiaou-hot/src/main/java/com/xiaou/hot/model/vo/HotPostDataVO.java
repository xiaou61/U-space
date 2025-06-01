package com.xiaou.hot.model.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class HotPostDataVO implements Serializable {

    /**
     * 标题
     */
    private String title;

    /**
     * 热度
     */
    private Integer followerCount;

    /**
     * 链接
     */
    private String url;


}