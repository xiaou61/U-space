package com.xiaou.word.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.word.domain.entity.UserWordRecord;
import com.xiaou.word.domain.entity.Word;
import com.xiaou.word.domain.req.WordReq;
import com.xiaou.word.domain.req.WordSelectReq;
import com.xiaou.word.domain.resp.UserWordRecordResp;
import com.xiaou.word.domain.resp.WordBatchResp;
import com.xiaou.word.domain.resp.WordResp;

import java.util.List;

public interface WordService extends IService<Word> {

    R<String> add(WordReq req);

    R<WordBatchResp> addBatch(List<WordReq> req);

    R<String> updateWord(String id, WordReq req);

    R<PageRespDto<WordResp>> listPage(PageReqDto req);

    List<WordResp> exportAllWords();

    R<PageRespDto<UserWordRecordResp>> pageUserSelect(PageReqDto req);

    R<PageRespDto<UserWordRecordResp>> pageUserSelectKnown(PageReqDto req);

    R<PageRespDto<UserWordRecordResp>> pageUserSelectUnknown(PageReqDto req);

}
