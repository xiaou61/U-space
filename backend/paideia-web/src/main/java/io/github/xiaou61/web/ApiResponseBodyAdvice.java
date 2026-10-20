package io.github.xiaou61.web;

import io.github.xiaou61.platform.ApiResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 把控制器返回值统一包装成 {@link ApiResponse}，并补上追踪标识。
 *
 * <p>三处必须跳过：{@code /actuator/**} 是运维端点，包装会破坏其既有响应结构；
 * {@code String} 返回值走的是 {@code StringHttpMessageConverter}，包装成对象会抛类型转换错误；
 * 非 JSON 响应（`byte[]`/`Resource` 这类文件下载）包装后响应体变成 JSON 对象、
 * Content-Type 与实际内容不符，客户端拿到的字节流就废了。
 * 骨架期不提供这三类接口，遇到时按原样返回，而不是悄悄改变响应类型。
 */
@RestControllerAdvice
public class ApiResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request, ServerHttpResponse response) {
        // byte[] 一律原样返回：它要么是文件下载（Content-Type 非 JSON，已在上一条被跳过），
        // 要么是**反代另一个服务**的原始响应体（WORK-028 的 Grafana 转发）。
        // 后者即使 Content-Type 是 JSON 也不能包装——它属于 Grafana，不属于本项目的 ApiResponse。
        if (isActuator(request) || body instanceof String || body instanceof byte[] || !isJson(selectedContentType)) {
            return body;
        }
        String traceId = traceIdOf(request);
        if (body instanceof ApiResponse<?> apiResponse) {
            // 控制器自己构造的响应也要补上追踪标识，否则这些接口的响应里 traceId 恒为 null，
            // 用户报错时拿不到可以对齐日志的标识
            return apiResponse.withTraceId(traceId);
        }
        return ApiResponse.ok(body).withTraceId(traceId);
    }

    private boolean isActuator(ServerHttpRequest request) {
        return request.getURI().getPath().startsWith("/actuator");
    }

    /** 只包装 JSON 响应；`application/problem+json` 这类后缀写法也算。 */
    private static boolean isJson(MediaType contentType) {
        return contentType != null
                && (MediaType.APPLICATION_JSON.includes(contentType) || contentType.getSubtype().endsWith("+json"));
    }

    private String traceIdOf(ServerHttpRequest request) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            Object value = servletRequest.getServletRequest().getAttribute(TraceIdFilter.TRACE_ID_ATTRIBUTE);
            return value == null ? null : value.toString();
        }
        return null;
    }
}
