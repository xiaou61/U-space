package com.xiaou.points.constant;

/**
 * 抽奖系统常量
 * 
 * @author xiaou
 */
public class LotteryConstants {
    
    /**
     * 单次抽奖成本（积分）
     */
    public static final int LOTTERY_COST = 100;
    
    /**
     * 平台期望回报率（75%）
     */
    public static final double TARGET_RETURN_RATE = 0.75;
    
    /**
     * 平台期望利润率（25%）
     */
    public static final double TARGET_PROFIT_RATE = 0.25;
    
    /**
     * 最大赔付率阈值（90%）
     */
    public static final double MAX_RETURN_RATE_THRESHOLD = 0.90;
    
    /**
     * Redis Key前缀
     */
    public static final String LOCK_USER_DRAW = "lottery:lock:user:";
    public static final String LOTTERY_LOCK_KEY = LOCK_USER_DRAW;
    public static final String LOTTERY_PRIZE_CONFIG_KEY = "lottery:prize:config";
    public static final String LOTTERY_PRIZE_STOCK_KEY = "lottery:prize:stock:";
    public static final String LOTTERY_STRATEGY_KEY = "lottery:strategy:current";
    
    /**
     * 抽奖限流配置
     */
    public static final int RATE_LIMIT_PER_MINUTE = 10;  // 每分钟最多10次
    public static final int RATE_LIMIT_PER_HOUR = 100;   // 每小时最多100次
    public static final int RATE_LIMIT_PER_DAY = 500;    // 每天最多500次
    public static final int MAX_DRAW_PER_DAY = 10;       // 每日最多抽奖次数
    public static final int DRAW_COST_POINTS = 100;      // 抽奖成本积分
    public static final int DRAW_COOLDOWN_SECONDS = 10;  // 抽奖冷却时间（秒）
    
    /**
     * 保底抽奖次数
     */
    public static final int GUARANTEE_COUNT = 20;
    
    /**
     * 概率调整相关
     */
    public static final int ADJUST_CHECK_INTERVAL = 100; // 每100次抽奖后检查调整
    public static final double MAX_ADJUST_RATIO = 0.2;   // 单次最大调整幅度20%
    public static final double PROBABILITY_TOLERANCE = 0.0001; // 概率归一化误差容忍度
    
    /**
     * 熔断触发条件
     */
    public static final int CONTINUOUS_HIGH_PRIZE_LIMIT = 10; // 连续10次高价值奖品触发熔断
}

