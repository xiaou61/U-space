package io.github.xiaou61.ai.internal.digest;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 速递的日期口径（2026-10-22）。
 *
 * <p>缺陷现场：`edition_date` 与接口的「今天」都取 UTC，业务时区是 UTC+8。
 * UTC 每 16:00（北京 00:00）日期翻篇，而采集在晚上才跑，于是页面有十来个小时必然是空的。
 * 这条测试钉死"口径来自配置时区"，防止以后有人顺手改回 UTC。
 */
class DigestZoneTest {

    @Test
    @DisplayName("默认按 Asia/Shanghai 判定今天，与 UTC 不是同一个日期")
    void todayFollowsConfiguredZone() {
        DigestProperties properties = new DigestProperties();
        assertThat(properties.zoneId()).isEqualTo(ZoneId.of("Asia/Shanghai"));
        // 跨时区的时刻：UTC 还是前一天，北京已经是新一天
        java.time.Instant crossingUtcMidnight = java.time.Instant.parse("2026-10-21T16:30:00Z");
        assertThat(crossingUtcMidnight.atZone(ZoneId.of("UTC")).toLocalDate())
                .isEqualTo(LocalDate.of(2026, 10, 21));
        assertThat(crossingUtcMidnight.atZone(properties.zoneId()).toLocalDate())
                .isEqualTo(LocalDate.of(2026, 10, 22));
    }

    @Test
    @DisplayName("时区配错时退回系统默认，不让应用起不来")
    void invalidZoneFallsBackInsteadOfThrowing() {
        DigestProperties properties = new DigestProperties();
        properties.setZone("Not/AZone");
        assertThat(properties.zoneId()).isNotNull();
        assertThat(properties.today()).isNotNull();
    }

    @Test
    @DisplayName("周报的「上周一」也按业务时区算")
    void weeklyWeekStartUsesSameZone() {
        LocalDate weekStart = DigestWeeklyService.lastWeekStart(ZoneId.of("Asia/Shanghai"));
        assertThat(weekStart.getDayOfWeek()).isEqualTo(java.time.DayOfWeek.MONDAY);
        assertThat(weekStart).isBefore(LocalDate.now(ZoneId.of("Asia/Shanghai")));
    }
}
