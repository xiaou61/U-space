package com.xiaou.excel.annotation;



import com.xiaou.common.utils.StringUtils;

import java.lang.annotation.*;

/**
 * 字典格式化
 *
 * @author Lion Li
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ExcelDictFormat {


    /**
     * 读取内容转表达式 (如: 0=男,1=女,2=未知)
     */
    String readConverterExp() default "";

    /**
     * 分隔符，读取字符串组内容
     */
    String separator() default StringUtils.SEPARATOR;

}
