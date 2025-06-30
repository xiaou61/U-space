package com.xiaou.system.log.domain.excel;


import cn.idev.excel.annotation.ExcelProperty;
import com.xiaou.excel.annotation.ExcelDictFormat;
import com.xiaou.system.log.domain.bo.SysOperLogBo;
import com.xiaou.system.log.domain.entity.SysOperLog;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;


/**
 * 操作日志记录视图对象 sys_oper_log
 */
@Data
@AutoMapper(target = SysOperLog.class)
public class SysOperLogExcelEntity implements Serializable {


    /**
     * 日志主键
     */
    @ExcelProperty(value = "日志主键")
    private Long operId;

    /**
     * 模块标题
     */
    @ExcelProperty(value = "模块标题")
    private String title;

    /**
     * 业务类型（0其它 1新增 2修改 3删除）
     */
    @ExcelProperty(value = "业务类型")
    @ExcelDictFormat(readConverterExp = "0=其他,1=新增,2=修改,3=删除")
    private Integer businessType;

    /**
     * 业务类型数组
     */
    @ExcelProperty(value = "业务类型")
    private Integer[] businessTypes;

    /**
     * 方法名称
     */
    @ExcelProperty(value = "方法名称")
    private String method;

    /**
     * 请求方式
     */
    @ExcelProperty(value = "请求方式")
    private String requestMethod;

    /**
     * 操作类别（0其它 1后台用户 2手机端用户）
     */
    @ExcelProperty(value = "操作类别")
    @ExcelDictFormat(readConverterExp = "0=其他,1=后台用户,2=手机端用户")
    private Integer operatorType;

    /**
     * 操作人员
     */
    @ExcelProperty(value = "操作人员")
    private String operName;

    /**
     * 请求URL
     */
    @ExcelProperty(value = "请求URL")
    private String operUrl;

    /**
     * 主机地址
     */
    @ExcelProperty(value = "主机地址")
    private String operIp;

    /**
     * 操作地点
     */
    @ExcelProperty(value = "操作地点")
    private String operLocation;

    /**
     * 请求参数
     */
    @ExcelProperty(value = "请求参数")
    private String operParam;

    /**
     * 返回参数
     */
    @ExcelProperty(value = "返回参数")
    private String jsonResult;

    /**
     * 操作状态（0正常 1异常）
     */
    @ExcelProperty(value = "操作状态")
    @ExcelDictFormat(readConverterExp = "0=正常,1=异常")
    private Integer status;

    /**
     * 错误消息
     */
    @ExcelProperty(value = "错误消息")
    private String errorMsg;

    /**
     * 操作时间
     */
    @ExcelProperty(value = "操作时间")
    private Date operTime;

    /**
     * 消耗时间
     */
    @ExcelProperty(value = "消耗时间")
    private Long costTime;
}
