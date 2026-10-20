package io.github.xiaou61.ops;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 代理路径拼接（WORK-028 AC-004 / T-009 的反向验证目标）。 */
class OpsGrafanaProxyTargetTest {

    private static final String BASE = "http://127.0.0.1:3000";

    @Test
    @DisplayName("前缀必须保留：Grafana 配了 serve_from_sub_path")
    void keepsTheSubPathPrefix() {
        // 剥掉前缀再转发时 Grafana 会回 301 要求带前缀，与代理互踢成死循环（实测空白页）
        assertThat(OpsGrafanaProxyController.targetUri(BASE, "/api/v1/ops/grafana/d/paideia-ops", null))
                .isEqualTo("http://127.0.0.1:3000/api/v1/ops/grafana/d/paideia-ops");
    }

    @Test
    @DisplayName("查询串必须带上——丢它会静默吃掉面板的时间范围与筛选")
    void keepsQueryString() {
        assertThat(OpsGrafanaProxyController.targetUri(
                        BASE, "/api/v1/ops/grafana/api/dashboards/uid/paideia-ops", "from=now-6h&to=now"))
                .isEqualTo("http://127.0.0.1:3000/api/v1/ops/grafana/api/dashboards/uid/paideia-ops"
                        + "?from=now-6h&to=now");
    }

    @Test
    @DisplayName("基址末尾斜杠不会产生双斜杠")
    void trimsTrailingSlashFromBase() {
        assertThat(OpsGrafanaProxyController.targetUri(BASE + "/", "/api/v1/ops/grafana/api/health", null))
                .isEqualTo("http://127.0.0.1:3000/api/v1/ops/grafana/api/health");
    }

    @Test
    @DisplayName("编码字符原样透传，不二次编码")
    void preservesEncodedCharacters() {
        assertThat(OpsGrafanaProxyController.targetUri(
                        BASE, "/api/v1/ops/grafana/d/paideia-ops", "var-unit=mysql%20server"))
                .isEqualTo("http://127.0.0.1:3000/api/v1/ops/grafana/d/paideia-ops?var-unit=mysql%20server");
    }

    @Test
    @DisplayName("重定向地址被改写为站内相对路径")
    void rewritesAbsoluteRedirectToRelative() {
        OpsGrafanaProxyController controller = new OpsGrafanaProxyController(new OpsProperties());
        // Grafana 按 root_url 生成绝对地址，浏览器连不到回环——必须改写成站内路径
        assertThat(controller.rewriteLocation("http://127.0.0.1:3000/api/v1/ops/grafana/d/paideia-ops?kiosk"))
                .isEqualTo("/api/v1/ops/grafana/d/paideia-ops?kiosk");
        // 与本代理无关的地址原样保留
        assertThat(controller.rewriteLocation("https://example.com/other"))
                .isEqualTo("https://example.com/other");
    }
}
