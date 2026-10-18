package io.github.xiaou61.account;

/**
 * 学习者身份（WORK-010）。
 *
 * <p><b>身份不是角色</b>——这是本项最重要的一条护栏（AC-001）：`role`（`learner` / `admin`）决定
 * **能做什么**，身份只决定**默认推荐什么、助手以什么形态出现**。所以身份**永远不许写进 `role`**，
 * 也不会改变任何一处权限判定。两者是两个正交的轴。
 *
 * <p><b>为什么是代码里的枚举，不是一张表</b>（design §1）：一期只有 {@link #JOB_SEEKER} 是"实心"的
 * ——它有画像字段、有推荐配置包、有面试面。其余几个只在枚举里**留位**、不做界面，
 * 因为"做一个自己都走不到的身份"只会多几处要维护的空壳。加第二个真身份时要一起补：
 * 它的画像字段归属、推荐配置包、身份默认 harness。
 *
 * <p><b>为什么放在账号模块包根</b>：它描述的是"这个人是谁"，与 `role` 同域；而 `AccountApi`
 * 已经是对外的身份读取入口，枚举与它放在一起，别的模块不必再依赖第二个模块。
 */
public enum Identity {

    /** 求职者：本项唯一实心的身份。 */
    JOB_SEEKER("job_seeker", "找工作"),

    /**
     * 以下三个是**留位**：只在枚举里存在，`selectable()` 为 false，界面上不出现。
     * 它们让"按身份配置"这件事有真实的第二个取值可测，而不必等到真做出来。
     */
    POSTGRAD("postgrad", "考研"),
    CIVIL_SERVICE("civil_service", "考公"),
    DAILY_LEARNING("daily_learning", "日常学习");

    private final String key;
    private final String label;

    Identity(String key, String label) {
        this.key = key;
        this.label = label;
    }

    /** 数据库与接口里的取值（小写蛇形）。 */
    public String key() {
        return key;
    }

    /** 界面上的名字。 */
    public String label() {
        return label;
    }

    /**
     * 一期能不能让用户选。
     *
     * <p>只有它把"身份"从一个概念变成**可验收的东西**：{@link #JOB_SEEKER} 有画像、有推荐配置、
     * 有面试面。其余三个选不选得动都看不出差别，放出来只会让人以为功能坏了。
     */
    public boolean selectable() {
        return this == JOB_SEEKER;
    }

    /** 库里读到不认识的值时退回默认身份，**不抛错**：一个脏值不该让整个页面打不开。 */
    public static Identity ofOrDefault(String raw) {
        if (raw == null) {
            return JOB_SEEKER;
        }
        String trimmed = raw.trim().toLowerCase(java.util.Locale.ROOT);
        for (Identity identity : values()) {
            if (identity.key.equals(trimmed)) {
                return identity;
            }
        }
        return JOB_SEEKER;
    }
}
