package io.github.xiaou61.ai.internal.camp;

/**
 * 一套卷子里三档难度各出几道（WORK-029）。
 *
 * <p><b>为什么不做自适应出题</b>：自适应要靠每道题的难度参数，而一个人十几道作答估不出参数。
 * 分档 + 平行卷是能诚实兑现的做法：两次卷的档位分布完全相同，于是"诊断 vs 复测"可比。
 *
 * <p><b>余数给中档而不是难档</b>：难档题最容易出坏题，题量不足时不该让难档占便宜。
 *
 * <p>纯函数。
 */
public final class TierDistribution {

    public record Tiers(int easy, int medium, int hard) {

        public int total() {
            return easy + medium + hard;
        }
    }

    private TierDistribution() {
    }

    public static Tiers split(int total) {
        if (total <= 0) {
            return new Tiers(0, 0, 0);
        }
        int easy = total / 3;
        int hard = total / 3;
        int medium = total - easy - hard;
        return new Tiers(easy, medium, hard);
    }
}
