package com.xiaou.campus.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class CampusGuideVO {

    private String title;

    private String content;

    private List<String> imageList;

    private List<String> keywords;

    private String category;



}
