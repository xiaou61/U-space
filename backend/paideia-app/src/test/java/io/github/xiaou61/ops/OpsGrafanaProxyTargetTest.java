package io.github.xiaou61.ops;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 代理路径拼接（WORK-028 AC-004 / T-009 的反向验证目标）。 */
class OpsGrafanaProxyTargetTest {

    private static final String BASE = "http://127.0.0.1:3000";

    @Test
    @DisplayName("子路径被保留，前缀被去掉")
    void keepsSubPath() {
        assertThat(OpsGrafanaProxyController.targetUri(BASE, "/api/v1/ops/grafana/d/paideia-ops", null))
                .isEqualTo("http://127.0.0.1:3000/d/paideia-ops");
    }

    @Test
    @DisplayName("查询串必须带上——丢它会静默吃掉面板的时间范围与筛选")
    void keepsQueryString() {
        assertThat(OpsGrafanaProxyController.targetUri(
                        BASE, "/api/v1/ops/grafana/api/dashboards/uid/paideia-ops", "from=now-6h&to=now"))
                .isEqualTo("http://127.0.0.1:3000/api/dashboards/uid/paideia-ops?from=now-6h&to=now");
    }

    @Test
    @DisplayName("前缀之后为空时落到根路径，不产生双斜杠")
    void emptyTailFallsBackToRoot() {
        assertThat(OpsGrafanaProxyController.targetUri(BASE, "/api/v1/ops/grafana", null))
                .isEqualTo("http://127.0.0.1:3000/");
        assertThat(OpsGrafanaProxyController.targetUri(BASE + "/", "/api/v1/ops/grafana/", null))
                .isEqualTo("http://127.0.0.1:3000/");
    }

    @Test
    @DisplayName("重定向地址被改写为站内相对路径")
    void rewritesAbsoluteRedirectToRelative() {
        OpsGrafanaProxyController controller = new OpsGrafanaProxyController(new OpsProperties());
        // Grafana 按 root_url 生成绝对地址，浏览器连不到回环——必须改写成站内路径。
        // 实测表现为面板 301 到一个不可达地址、页面一片空白。
        assertThat(controller.rewriteLocation("http://127.0.0.1:3000/api/v1/ops/grafana/d/paideia-ops?kiosk"))
                .isEqualTo("/api/v1/ops/grafana/d/paideia-ops?kiosk");
        // 与本代理无关的地址原样保留
        assertThat(controller.rewriteLocation("https://example.com/other"))
                .isEqualTo("https://example.com/other");
    }

    @Test
    @DisplayName("编码字符原样透传，不二次编码")
    void preservesEncodedCharacters() {
        assertThat(OpsGrafanaProxyController.targetUri(
                        BASE, "/api/v1/ops/grafana/d/paideia-ops", "var-unit=mysql%20server"))
                .isEqualTo("http://127.0.0.1:3000/d/paideia-ops?var-unit=mysql%20server");
    }
}
