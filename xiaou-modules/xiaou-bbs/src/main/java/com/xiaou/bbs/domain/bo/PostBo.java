package com.xiaou.bbs.domain.bo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaou.bbs.domain.entity.Post;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 帖子表BO
 */
@Data
@AutoMapper(target = Post.class)
public class PostBo {

    /**
     * 帖子标题
     */
    @NotBlank(message = "帖子标题不能为空")
    private String title;

    /**
     * 帖子内容
     */
    @NotBlank(message = "帖子内容不能为空")
    private String content;

    private List<String> imageUrls;

    @NotBlank(message = "帖子分类不能为空")
    private String category;

}