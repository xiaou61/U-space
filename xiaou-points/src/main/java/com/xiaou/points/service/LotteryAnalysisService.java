package com.xiaou.points.service;

import com.xiaou.points.dto.lottery.admin.AnalysisResponse;

/**
 * 抽奖数据分析服务接口
 * 
 * @author xiaou
 */
public interface LotteryAnalysisService {
    
    /**
     * 获取综合分析数据
     */
    AnalysisResponse getComprehensiveAnalysis(String startDate, String endDate);
}

