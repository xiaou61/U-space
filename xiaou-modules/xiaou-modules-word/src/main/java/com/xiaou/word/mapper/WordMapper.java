package com.xiaou.word.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaou.word.domain.entity.Word;
import com.xiaou.word.domain.resp.WordResp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;

@Mapper
public interface WordMapper extends BaseMapper<Word> {

    @Select("SELECT * FROM u_word WHERE id IN (#{wordIds})")
    List<WordResp> listByIds(Set<String> wordIds);
}




