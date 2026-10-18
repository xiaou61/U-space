package io.github.xiaou61.ai.internal.provider;

import io.agentscope.core.model.transport.HttpRequest;
import io.agentscope.core.model.transport.HttpResponse;
import io.agentscope.core.model.transport.HttpTransport;
import io.agentscope.core.model.transport.HttpTransportException;
import io.agentscope.core.model.transport.HttpTransportConfig;
import io.agentscope.core.model.transport.OkHttpTransport;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 每次都校验出站地址的传输层。
 *
 * <p>为什么要在这一层做而不是只在校验用户填的 baseUrl 时做：模型客户端可能请求它自己拼出来
 * 的地址（拼路径、跟随重定向），只信任"用户填的那个串"是不够的。这里在**真正发出请求之前**
 * 拿到最终 URL 再校验一次——它是 SSRF 防线的第二道，也是唯一能看见最终地址的一道。
 *
 * <p>用装饰而不是自写传输实现：出站细节（连接池、超时、代理）交给 AgentScope 的默认实现，
 * 这里只加一道闸。
 */
@Component
public class GuardedHttpTransport implements HttpTransport {

    private final UpstreamGuard guard;
    /**
     * 禁止跟随重定向。
     *
     * <p>设计文档与 {@link UpstreamGuard} 都把"禁重定向"列为 SSRF 的缓解手段之一：允许跟随的话，
     * 一个公网地址可以 302 到内网，而第二道闸只看得到**用户填的那个地址**。
     * OkHttp 默认 `followRedirects=true`，所以必须显式关掉。
     */
    private final OkHttpTransport delegate = new OkHttpTransport(
            new okhttp3.OkHttpClient.Builder()
                    .followRedirects(false)
                    .followSslRedirects(false)
                    .build(),
            HttpTransportConfig.defaults());

    public GuardedHttpTransport(UpstreamGuard guard) {
        this.guard = guard;
    }

    @Override
    public HttpResponse execute(HttpRequest request) throws HttpTransportException {
        guard.assertAllowed(request.getUrl());
        return delegate.execute(request);
    }

    @Override
    public Flux<String> stream(HttpRequest request) {
        guard.assertAllowed(request.getUrl());
        return delegate.stream(request);
    }

    @Override
    public void close() {
        delegate.close();
    }
}
