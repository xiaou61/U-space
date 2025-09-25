package com.xiaou.points.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 打卡日历查询请求DTO
 * 
 * @author xiaou
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckinCalendarRequest {
    
    /**
     * 年月，格式：YYYY-MM
     * 如果为空则返回当前月份
     */
    private String yearMonth;
}
