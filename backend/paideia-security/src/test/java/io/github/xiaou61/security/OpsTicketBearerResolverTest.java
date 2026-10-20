package io.github.xiaou61.security;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * 查询串票据的作用范围（WORK-028）。
 *
 * <p>这条纪律是这套做法的安全边界：**只有 Grafana 反代前缀**接受 `?ticket=`，
 * 别处一律只认 `Authorization` 头。范围一旦放宽，令牌就会从一处已知代价扩散到全站 URL。
 */
class OpsTicketBearerResolverTest {

    @Test
    @DisplayName("反代前缀接受查询串票据")
    void acceptsTicketOnProxyPrefix() {
        HttpServletRequest request = request("/api/v1/ops/grafana/d/paideia-ops", "ticket=abc");
        assertThat(OpsTicketBearerResolver.INSTANCE.resolve(request)).isEqualTo("abc");
    }

    @Test
    @DisplayName("其它路径忽略查询串票据")
    void ignoresTicketElsewhere() {
        HttpServletRequest request = request("/api/v1/knowledge/entries", "ticket=abc");
        assertThat(OpsTicketBearerResolver.INSTANCE.resolve(request)).isNull();
    }

    @Test
    @DisplayName("Authorization 头优先于查询串")
    void headerWins() {
        HttpServletRequest request = request("/api/v1/ops/grafana/d/paideia-ops", "ticket=abc");
        Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer from-header");
        assertThat(OpsTicketBearerResolver.INSTANCE.resolve(request)).isEqualTo("from-header");
    }

    @Test
    @DisplayName("前缀之外即使只有头也照常工作（这是原来的路径，不能弄坏）")
    void headerStillWorksOutsidePrefix() {
        HttpServletRequest request = request("/api/v1/me", null);
        Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer normal");
        assertThat(OpsTicketBearerResolver.INSTANCE.resolve(request)).isEqualTo("normal");
    }

    private static HttpServletRequest request(String uri, String query) {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getRequestURI()).thenReturn(uri);
        if (query != null) {
            String[] parts = query.split("=", 2);
            Mockito.when(request.getParameter(parts[0])).thenReturn(parts.length > 1 ? parts[1] : "");
        }
        return request;
    }
}
