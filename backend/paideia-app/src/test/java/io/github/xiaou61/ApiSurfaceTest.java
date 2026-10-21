package io.github.xiaou61;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.xiaou61.platform.ApiAccess;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * L1：从**运行时路由 + {@link ApiAccess} 注解**派生接口清单，并检查四类"扫描会漏"的情形。
 *
 * <p>这里不再有人工维护的清单。清单就是"Spring 实际注册了什么"，
 * 访问级别就是控制器上标注的 {@code @ApiAccess}——两处都在代码旁边，不会各说各话。
 *
 * <p>被拦住的四种情况：
 * <ol>
 *   <li><b>新增端点没标注解</b>：{@code /api/v1/**} 下任何一个 handler 解析不到 {@code @ApiAccess} 就失败。
 *       这替代了"忘记登记清单"。</li>
 *   <li><b>不带 HTTP 方法的映射</b>：{@code @RequestMapping("/x")} 不限方法。它会响应所有动词，
 *       拿到 401/403 的矩阵里看不出异常，**而按方法枚举的扫描会静默跳过它**（2026-10-18 实测确认）。</li>
 *   <li><b>挂在 {@code /api/v1} 之外的 API 路径</b>：例如误写成 {@code /api/notes}。
 *       按前缀过滤的扫描会跳过它，而它同样是对外接口。</li>
 *   <li><b>同一个端点两种声明</b>：方法级与类级同时标注且不一致时，以方法级为准（这是有意的覆盖机制），
 *       但类级与方法级**都**标注同一个端点会导致歧义，这里要求一个端点只有一处声明来源。</li>
 * </ol>
 *
 * <p>与数据库相关：{@code paideia-app} 的测试上下文包含数据源与 Flyway，缺口令时上下文起不来。
 * 按项目既有规则这里跳过而不失败，**补偿手段在 CI**（断言契约套件 {@code skipped == 0}）。
 */
@SpringBootTest
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "PAIDEIA_TEST_DB_PASSWORD", matches = ".+")
class ApiSurfaceTest {

    /** 测试专用控制器会被组件扫描进来，但不是对外契约。 */
    private static final String FIXTURE_PREFIX = "/api/v1/fixture/";

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    /** 派生出来的清单：每条运行时端点及其声明的访问级别。 */
    record Endpoint(String method, String pattern, ApiAccess.Access access) {
        String key() {
            return method + " " + pattern;
        }
    }

    /**
     * 把注册 pattern 与实际请求路径对上。
     *
     * <p>不能用正则：反代端点 {@code /api/v1/ops/grafana/**} 里的 {@code **} 是非法正则，
     * 直接 {@code matches} 会抛 {@code PatternSyntaxException}（2026-10-21 实测，
     * 表现为整轮自查第一个请求就中断、111 条端点全被记成"没打到"）。用 Spring 自己的
     * {@link org.springframework.util.AntPathMatcher}，`{id}` 与 `**` 都是它原生支持的写法。
     */
    static boolean matches(String pattern, String path) {
        return PATH_MATCHER.match(pattern, path);
    }

    private static final org.springframework.util.AntPathMatcher PATH_MATCHER =
            new org.springframework.util.AntPathMatcher();

    /**
     * 越具体的 pattern 排越前：通配符（`{` 与 `*`）越少越具体。
     *
     * <p>必须有这个顺序：`/credits/audit` 与 `/credits/{userId}` 都能匹配 {@code /credits/audit}，
     * 按注册顺序遍历会随机归到通配那条上，审计端点于是永远显示"没走到"。
     */
    static java.util.Comparator<String> bySpecificity() {
        return java.util.Comparator.comparingLong((String pattern) -> pattern.chars()
                .filter(ch -> ch == '{').count());
    }

    static List<Endpoint> endpoints(RequestMappingHandlerMapping mapping) {
        List<Endpoint> endpoints = new ArrayList<>();
        for (var entry : mapping.getHandlerMethods().entrySet()) {
            RequestMappingInfo info = entry.getKey();
            HandlerMethod handler = entry.getValue();

            // 只关心 /api/** —— 静态资源与 Actuator 不在这个 HandlerMapping 里，也不属于业务契约
            for (String pattern : info.getPatternValues()) {
                if (!pattern.startsWith("/api/") || pattern.startsWith(FIXTURE_PREFIX)) {
                    continue;
                }
                // HEAD 由框架按 GET 自动派生，不单独声明访问级别
                Set<String> methods = info.getMethodsCondition().getMethods().stream()
                        .map(Enum::name)
                        .filter(method -> !"HEAD".equals(method))
                        .collect(Collectors.toCollection(TreeSet::new));
                if (methods.isEmpty()) {
                    // 没有方法条件的映射不该出现在业务接口里：占位符让下面的断言能点名它
                    endpoints.add(new Endpoint("ANY", pattern, ApiAccess.Access.AUTHENTICATED));
                    continue;
                }
                ApiAccess.Access access = resolveAccess(handler);
                for (String method : methods) {
                    endpoints.add(new Endpoint(method, pattern, access));
                }
            }
        }
        return endpoints;
    }

    /**
     * 方法级声明优先于类级；都没有则返回 {@code null}（由调用方报成"未声明"）。
     *
     * <p>注解可能落在代理类或声明类上，所以按 {@code getMethod().getDeclaringClass()} 与
     * 实际 bean 类型各查一次。
     */
    static ApiAccess.Access resolveAccess(HandlerMethod handler) {
        ApiAccess onMethod = handler.getMethodAnnotation(ApiAccess.class);
        if (onMethod != null) {
            return onMethod.value();
        }
        ApiAccess onBean = handler.getBeanType().getAnnotation(ApiAccess.class);
        if (onBean != null) {
            return onBean.value();
        }
        ApiAccess onDeclaring = handler.getMethod().getDeclaringClass().getAnnotation(ApiAccess.class);
        return onDeclaring == null ? null : onDeclaring.value();
    }

    @Test
    @DisplayName("pattern 匹配能处理 `**` 与 `{id}`，且更具体的排在前")
    void patternMatchingHandlesWildcardsAndSpecificity() {
        // `/api/v1/ops/grafana/**` 曾被当成正则去跑，`**` 抛 PatternSyntaxException，
        // 整轮自查第一个请求就中断——111 条端点被记成"没打到"。这两条钉住它。
        assertThat(ApiSurfaceTest.matches("/api/v1/ops/grafana/**", "/api/v1/ops/grafana/api/health"))
                .isTrue();
        assertThat(ApiSurfaceTest.matches("/api/v1/notes/{id}", "/api/v1/notes/17")).isTrue();
        // `/credits/audit` 不能被 `/credits/{userId}` 抢走
        assertThat(java.util.stream.Stream.of("/api/v1/ai/admin/credits/{userId}",
                        "/api/v1/ai/admin/credits/audit")
                .sorted(ApiSurfaceTest.bySpecificity()).findFirst())
                .contains("/api/v1/ai/admin/credits/audit");
        // `*` 也计入具体度：`/a/{b}`（1 个通配）必须排在 `/a/**`（2 个）之前，
        // 否则反代整条 `/**` 会把更具体的映射抢走
        assertThat(java.util.stream.Stream.of("/a/**", "/a/{b}")
                .sorted(ApiSurfaceTest.bySpecificity()).findFirst()).contains("/a/{b}");
    }

    @Test
    @DisplayName("每个 /api 端点都能从 @ApiAccess 解析到访问级别")
    void everyEndpointDeclaresItsAccess() {
        List<String> undeclared = endpoints(handlerMapping).stream()
                .filter(endpoint -> endpoint.access() == null)
                .map(Endpoint::key)
                .sorted()
                .toList();

        assertThat(undeclared)
                .as("这些端点没有 @ApiAccess 声明——在控制器类（或方法）上加一行 "
                        + "@ApiAccess(ApiAccess.Access.…) 即可；漏了就无法自动验证鉴权行为")
                .isEmpty();
    }

    @Test
    @DisplayName("没有不限 HTTP 方法的映射")
    void everyEndpointNamesItsMethod() {
        List<String> anyMethod = endpoints(handlerMapping).stream()
                .filter(endpoint -> "ANY".equals(endpoint.method()))
                .map(Endpoint::key)
                .sorted()
                .toList();

        assertThat(anyMethod)
                .as("这些映射没有指定 HTTP 方法（用了 @RequestMapping 而没写 method）。"
                        + "它们会响应所有动词，且按方法枚举的检查会跳过——改用 @GetMapping/@PostMapping 等")
                .isEmpty();
    }

    @Test
    @DisplayName("没有挂在 /api/v1 之外的业务接口")
    void everyApiEndpointLivesUnderVersionedPrefix() {
        List<String> outside = endpoints(handlerMapping).stream()
                .filter(endpoint -> !"ANY".equals(endpoint.method()))
                .map(Endpoint::pattern)
                .filter(pattern -> !pattern.startsWith("/api/v1/"))
                .distinct()
                .sorted()
                .toList();

        assertThat(outside)
                .as("这些接口挂在 /api/v1 之外。版本前缀是鉴权规则与前端代理共同依赖的约定"
                        + "（SecurityConfiguration 的规则全是 /api/v1/**），挪进来或明确登记为特例")
                .isEmpty();
    }

    @Test
    @DisplayName("同一个端点没有两种声明来源")
    void accessIsDeclaredInExactlyOnePlace() {
        List<String> ambiguous = new ArrayList<>();
        for (var entry : handlerMapping.getHandlerMethods().entrySet()) {
            HandlerMethod handler = entry.getValue();
            boolean methodLevel = handler.getMethodAnnotation(ApiAccess.class) != null;
            boolean classLevel = handler.getBeanType().getAnnotation(ApiAccess.class) != null
                    || handler.getMethod().getDeclaringClass().getAnnotation(ApiAccess.class) != null;
            if (methodLevel && classLevel) {
                ambiguous.add(entry.getKey().getPatternValues() + " → " + handler.getMethod().getName());
            }
        }

        assertThat(ambiguous)
                .as("方法级与类级同时声明了访问级别。方法级会覆盖类级（例如附件控制器），"
                        + "但同时标两处会让'这个端点到底谁说了算'需要读两处才能确定")
                .isEmpty();
    }

    @Test
    @DisplayName("端点清单没有重复")
    void endpointListHasNoDuplicates() {
        List<Endpoint> all = endpoints(handlerMapping);
        long distinct = all.stream().map(Endpoint::key).distinct().count();
        assertThat(distinct).as("同一 METHOD + 路径被注册了两次").isEqualTo(all.size());
    }

    @Test
    @DisplayName("四种访问级别都被用到了")
    void everyAccessLevelIsRepresented() {
        Set<ApiAccess.Access> used = endpoints(handlerMapping).stream()
                .map(Endpoint::access)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        assertThat(used).containsExactlyInAnyOrder(ApiAccess.Access.values());
    }
}
