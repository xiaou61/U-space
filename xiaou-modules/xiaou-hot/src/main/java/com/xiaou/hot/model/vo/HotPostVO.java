package com.xiaou.hot.model.vo;

import com.alibaba.fastjson2.JSON;
import com.xiaou.hot.model.enums.CategoryTypeEnum;
import com.xiaou.hot.model.po.HotPost;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class HotPostVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 排行榜名称
     */
    private String name;

    /**
     * 热点类型
     */
    private String type;

    /**
     * 热点类型名称
     */
    private String typeName;

    /**
     * 图标地址
     */
    private String iconUrl;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 分类
     */
    private Integer category;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 数据
     */
    private List<HotPostDataVO> data;

    /**
     * 对象转包装类
     *
     * @param hotPost 热门帖子
     * @return {@link HotPostVO }
     */
    public static HotPostVO objToVo(HotPost hotPost) {
        if (hotPost == null) {
            return null;
        }
        HotPostVO hotPostVO = new HotPostVO();
        hotPostVO.setId(hotPost.getId());
        hotPostVO.setName(hotPost.getName());
        hotPostVO.setType(hotPost.getType());
        hotPostVO.setTypeName(hotPost.getTypeName());
        hotPostVO.setIconUrl(hotPost.getIconUrl());
        hotPostVO.setUpdateTime(hotPost.getUpdateTime());
        hotPostVO.setCategory(hotPost.getCategory());
        hotPostVO.setCategoryName(CategoryTypeEnum.getEnumByValue(hotPost.getCategory()).getText());
        hotPostVO.setData(JSON.parseArray(hotPost.getHostJson(), HotPostDataVO.class));
        return hotPostVO;
    }

}
