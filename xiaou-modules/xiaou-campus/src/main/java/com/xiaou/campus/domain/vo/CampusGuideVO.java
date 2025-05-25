package com.xiaou.campus.domain.vo;

import com.alibaba.fastjson2.JSON;
import com.xiaou.campus.domain.entity.CampusGuide;
import lombok.Data;

import java.util.List;

@Data
public class CampusGuideVO {

    private Long id;

    private String title;

    private String content;

    private List<String> imageList;

    private List<String> keywords;

    private String category;

    public static CampusGuideVO fromEntity(CampusGuide entity) {
        CampusGuideVO vo = new CampusGuideVO();
        vo.setId(entity.getId());
        vo.setTitle(entity.getTitle());
        vo.setContent(entity.getContent());
        vo.setCategory(entity.getCategory());
        if (entity.getImageList() != null) {
            vo.setImageList(JSON.parseArray(entity.getImageList(), String.class));
        }
        if (entity.getKeywords() != null) {
            vo.setKeywords(JSON.parseArray(entity.getKeywords(), String.class));
        }
        return vo;
    }


}
