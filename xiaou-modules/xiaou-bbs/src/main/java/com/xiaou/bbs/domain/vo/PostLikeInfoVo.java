package com.xiaou.bbs.domain.vo;

import lombok.Data;

import java.util.List;
@Data
public class PostLikeInfoVo {
    private Long postId;
    private String title;
    private String content;
    private List<String> imageUrls;
    private String category;
    private Boolean liked = true;
}
