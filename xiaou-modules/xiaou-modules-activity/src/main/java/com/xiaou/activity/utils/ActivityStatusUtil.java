package com.xiaou.activity.utils;

import com.xiaou.activity.domain.entity.Activity;

import java.util.Date;
import java.util.Objects;

/**
 * 活动状态计算工具类
 */
public class ActivityStatusUtil {

    /**
     * 状态常量定义
     */
    public static final int STATUS_PENDING = 0;    // 待开始
    public static final int STATUS_ONGOING = 1;    // 进行中
    public static final int STATUS_FINISHED = 2;   // 已结束
    public static final int STATUS_CANCELLED = 3;  // 已取消
    public static final int STATUS_DISABLED = 4;   // 已禁用（新增：管理员禁用，但可以重新启用）

    /**
     * 根据当前时间和活动时间计算活动状态
     * 注意：已取消(3)和已禁用(4)的状态不会被自动改变
     * 
     * @param activity 活动信息
     * @return 计算出的状态
     */
    public static Integer calculateStatus(Activity activity) {
        if (activity == null) {
            return null;
        }

        // 如果活动已被取消或禁用，状态不会改变
        Integer currentStatus = activity.getStatus();
        if (currentStatus != null && (currentStatus == STATUS_CANCELLED || currentStatus == STATUS_DISABLED)) {
            return currentStatus;
        }

        Date now = new Date();
        Date startTime = activity.getStartTime();
        Date endTime = activity.getEndTime();

        // 时间校验
        if (startTime == null || endTime == null) {
            return STATUS_PENDING; // 时间未设置完整，默认为待开始
        }

        // 根据时间计算状态
        if (now.before(startTime)) {
            return STATUS_PENDING;  // 待开始
        } else if (now.before(endTime)) {
            return STATUS_ONGOING;  // 进行中
        } else {
            return STATUS_FINISHED; // 已结束
        }
    }

    /**
     * 获取状态名称
     * 
     * @param status 状态值
     * @return 状态名称
     */
    public static String getStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case STATUS_PENDING: return "待开始";
            case STATUS_ONGOING: return "进行中";
            case STATUS_FINISHED: return "已结束";
            case STATUS_CANCELLED: return "已取消";
            case STATUS_DISABLED: return "已禁用";
            default: return "未知";
        }
    }

    /**
     * 判断活动是否可以参与
     * 只有进行中的活动才能参与
     * 
     * @param activity 活动信息
     * @return true-可以参与，false-不能参与
     */
    public static boolean canParticipate(Activity activity) {
        Integer status = calculateStatus(activity);
        return Objects.equals(status, STATUS_ONGOING);
    }

    /**
     * 判断活动是否可以编辑
     * 已结束和已取消的活动不能编辑
     * 
     * @param activity 活动信息
     * @return true-可以编辑，false-不能编辑
     */
    public static boolean canEdit(Activity activity) {
        Integer status = calculateStatus(activity);
        return !Objects.equals(status, STATUS_FINISHED) && !Objects.equals(status, STATUS_CANCELLED);
    }

    /**
     * 判断活动是否已经结束
     * 
     * @param activity 活动信息
     * @return true-已结束，false-未结束
     */
    public static boolean isFinished(Activity activity) {
        Integer status = calculateStatus(activity);
        return Objects.equals(status, STATUS_FINISHED);
    }

    /**
     * 判断活动是否处于有效状态（未取消且未禁用）
     * 
     * @param activity 活动信息
     * @return true-有效，false-无效
     */
    public static boolean isValid(Activity activity) {
        Integer status = calculateStatus(activity);
        return !Objects.equals(status, STATUS_CANCELLED) && !Objects.equals(status, STATUS_DISABLED);
    }
} 