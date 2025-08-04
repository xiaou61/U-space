package com.xiaou.word.domain.excel;

import cn.idev.excel.annotation.ExcelProperty;
import com.xiaou.excel.annotation.ExcelDictFormat;
import com.xiaou.excel.convert.ExcelDictConvert;
import lombok.Data;

/**
 * 单词Excel导入模板
 */
@Data
public class WordExcelDto {

    @ExcelProperty("单词*")
    private String word;

    @ExcelProperty("中文释义*")
    private String definition;

    @ExcelProperty("音标")
    private String phonetic;

    @ExcelProperty("词性")
    private String partOfSpeech;

    @ExcelProperty("英文释义")
    private String definitionEn;

    @ExcelProperty("例句")
    private String exampleSentence;

    @ExcelProperty("词库来源")
    private String source;

    @ExcelProperty("标签")
    private String tags;

    @ExcelProperty(value = "难度等级", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "1=1级(简单),2=2级(较易),3=3级(中等),4=4级(较难),5=5级(困难)")
    private Integer difficulty;

    @ExcelProperty(value = "状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "1=启用,0=禁用")
    private Integer status;
}