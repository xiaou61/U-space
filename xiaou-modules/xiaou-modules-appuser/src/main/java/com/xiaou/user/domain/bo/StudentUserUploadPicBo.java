package com.xiaou.user.domain.bo;

import com.xiaou.user.domain.entity.StudentUser;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@AutoMapper(target = StudentUser.class)
public class StudentUserUploadPicBo {
    /**
     * id
     */
    @NotBlank(message = "id不能为空")
    private String id;

    /**
     * 头像
     */
    @NotBlank(message = "头像不能为空")
    private String avatarUrl;

}
