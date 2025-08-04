package com.xiaou.word.controller;

import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.excel.util.ExcelUtils;
import com.xiaou.word.domain.excel.WordExcelDto;
import com.xiaou.word.domain.req.WordReq;
import com.xiaou.word.domain.resp.WordBatchResp;
import com.xiaou.word.domain.resp.WordResp;
import com.xiaou.word.service.UserWordRecordService;
import com.xiaou.word.service.WordService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/word")
@Validated
public class WordAdminController {
    @Resource
    private WordService wordService;

    /**
     * 添加单词
     * 
     * @param req
     * @return
     */
    @PostMapping("/add")
    public R<String> add(@RequestBody WordReq req) {
        return wordService.add(req);
    }

    /**
     * 批量添加单词
     */
    @PostMapping("/addBatch")
    public R<WordBatchResp> addBatch(@RequestBody List<WordReq> req) {
        return wordService.addBatch(req);
    }

    /**
     * 修改一个单词
     */
    @PostMapping("/update")
    public R<String> update(@RequestParam String id, @RequestBody WordReq req) {
        return wordService.updateWord(id, req);
    }

    /**
     * 删除一个单词
     */
    @DeleteMapping("/delete")
    public R<String> delete(@RequestParam String id) {
        return wordService.removeById(id) ? R.ok("删除成功") : R.fail("删除失败");
    }

    /**
     * 分页查看单词 管理员接口
     */
    @PostMapping("/page")
    public R<PageRespDto<WordResp>> page(@RequestBody PageReqDto req) {
        return wordService.listPage(req);
    }

    /**
     * 导出所有单词到Excel
     */
    @GetMapping("/exportExcel")
    public void exportExcel(HttpServletResponse response) {
        try {
            List<WordResp> words = wordService.exportAllWords();

            // 转换为Excel导出格式
            List<WordExcelDto> excelData = words.stream().map(word -> {
                WordExcelDto dto = new WordExcelDto();
                dto.setWord(word.getWord());
                dto.setDefinition(word.getDefinition());
                dto.setPhonetic(word.getPhonetic());
                dto.setPartOfSpeech(word.getPartOfSpeech());
                dto.setDefinitionEn(word.getDefinitionEn());
                dto.setExampleSentence(word.getExampleSentence());
                dto.setSource(word.getSource());
                dto.setTags(word.getTags());
                dto.setDifficulty(word.getDifficulty());
                dto.setStatus(word.getStatus());
                return dto;
            }).toList();

            String fileName = "单词数据导出_" + System.currentTimeMillis();
            ExcelUtils.write(response, excelData, WordExcelDto.class, fileName, "单词数据");
        } catch (Exception e) {
            log.error("导出Excel失败", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
