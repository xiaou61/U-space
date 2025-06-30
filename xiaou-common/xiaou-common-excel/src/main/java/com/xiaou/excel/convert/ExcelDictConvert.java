package com.xiaou.excel.convert;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;

import cn.idev.excel.converters.Converter;
import cn.idev.excel.enums.CellDataTypeEnum;
import cn.idev.excel.metadata.GlobalConfiguration;
import cn.idev.excel.metadata.data.ReadCellData;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;
import com.xiaou.excel.annotation.ExcelDictFormat;
import com.xiaou.excel.util.FastExcelUtil;
import lombok.extern.slf4j.Slf4j;


import java.lang.reflect.Field;

/**
 * 字典格式化转换处理（仅使用表达式，不依赖字典服务）
 * 
 * 示例注解使用：
 * @ExcelProperty(value = "业务类型", converter = ExcelDictConvert.class)
 * @ExcelDictFormat(readConverterExp = "0=其它,1=新增,2=修改,3=删除")
 */
@Slf4j
public class ExcelDictConvert implements Converter<Object> {

    @Override
    public Class<Object> supportJavaTypeKey() {
        return Object.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return null;
    }

    @Override
    public Object convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        ExcelDictFormat anno = getAnnotation(contentProperty.getField());
        String label = cellData.getStringValue();
        String value = FastExcelUtil.reverseByExp(label, anno.readConverterExp(), anno.separator());
        return Convert.convert(contentProperty.getField().getType(), value);
    }

    @Override
    public WriteCellData<String> convertToExcelData(Object object, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        if (ObjectUtil.isNull(object)) {
            return new WriteCellData<>("");
        }
        ExcelDictFormat anno = getAnnotation(contentProperty.getField());
        String value = Convert.toStr(object);
        String label = FastExcelUtil.convertByExp(value, anno.readConverterExp(), anno.separator());
        return new WriteCellData<>(label);
    }

    private ExcelDictFormat getAnnotation(Field field) {
        return AnnotationUtil.getAnnotation(field, ExcelDictFormat.class);
    }
}
