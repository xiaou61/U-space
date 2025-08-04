package com.xiaou.word.domain.resp;

import com.xiaou.word.domain.entity.Word;
import lombok.Data;

import java.util.List;

@Data
/**
 * 批量添加单词返回结果
 */
public class WordBatchResp {
    //成功添加的单词
    private List<String> success;
    //添加失败的单词
    private List<String> fail;
}
