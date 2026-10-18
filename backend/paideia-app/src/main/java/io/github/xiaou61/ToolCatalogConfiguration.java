package io.github.xiaou61;

import io.github.xiaou61.platform.PaideiaTool;
import io.github.xiaou61.platform.ToolCatalog;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 工具目录的装配（WORK-019）。
 *
 * <p><b>为什么 Bean 声明在装配根而不在 platform</b>：`paideia-platform` 是纯契约库，**不引 Spring**
 * （它的依赖只有 `spring-modulith-api` 与测试用的 JUnit/AssertJ）；而目录的校验必须在**启动期**
 * 发生——重名、非法名、schema 不对都要让应用起不来。放在这里两边都满足。
 *
 * <p>Spring 会把所有模块里的 {@link PaideiaTool} Bean 收进来：工具实现留在拥有数据的模块里，
 * 这里只是把它们汇到一处。
 */
@Configuration
public class ToolCatalogConfiguration {

    @Bean
    ToolCatalog toolCatalog(List<PaideiaTool> tools) {
        return new ToolCatalog(tools);
    }
}
