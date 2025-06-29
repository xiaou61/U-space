package com.xiaou.auth.admin.domain.excel;


import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 班级 Excel 导入导出模型
 */
@Data
public class ClassEntityExcel {

    @ExcelProperty("班级名称")
    private String className;

    @ExcelProperty("年级")
    private String grade;

    @ExcelProperty("所属专业")
    private String major;

    @ExcelProperty("班主任姓名")
    private String classTeacher;

    @ExcelProperty("学生人数")
    private Integer studentCount;
}
