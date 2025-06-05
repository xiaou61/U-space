package com.xiaou.domain.bo;// VoteCreateRequest.java

import com.xiaou.domain.entity.Vote;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AutoMapper(target = Vote.class)
public class VoteCreateRequest {
    @NotBlank
    private String title;

    private String description;

    private boolean isMultiple;

    private boolean isAnonymous;

    private Integer maxChoices;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @NotEmpty
    private List<@NotBlank String> options; // 选项内容
}
