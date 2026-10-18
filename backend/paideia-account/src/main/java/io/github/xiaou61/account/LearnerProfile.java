package io.github.xiaou61.account;

import java.time.Instant;

/**
 * 学习者画像与当前身份（WORK-010）。
 *
 * <p>它是"这个人现在是谁"的载体：当前身份 + 求职者专用的四个画像字段（都可空）。
 *
 * <p><b>与 {@code role} 无关</b>：这个对象里的任何字段都**不参与权限判定**。管理端能不能进、
 * 能不能写知识库，仍然只看 `role`（AC-001）。把它当成"第二套权限"是最容易犯的误读。
 *
 * @param identity   当前身份；库里读到不认识的值时已被规范成默认身份
 * @param targetRole 目标岗位（求职者专用，可空）
 * @param targetCity 目标城市（求职者专用，可空）
 * @param timeline   秋招 / 春招（求职者专用，可空）
 * @param stage      投递 / 笔试 / 面试（求职者专用，可空）
 */
public record LearnerProfile(Identity identity, String targetRole, String targetCity,
                             String timeline, String stage, Instant updatedAt) {

    /** 新用户/没有画像行时的取值：默认身份、四个画像字段都空。 */
    public static LearnerProfile defaults() {
        return new LearnerProfile(Identity.JOB_SEEKER, null, null, null, null, null);
    }
}
