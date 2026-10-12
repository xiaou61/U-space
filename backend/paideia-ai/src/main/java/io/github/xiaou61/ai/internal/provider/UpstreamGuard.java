package io.github.xiaou61.ai.internal.provider;

import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import org.springframework.stereotype.Component;

/**
 * 出站地址校验（SSRF 防护）。
 *
 * <p>为什么要它：baseUrl 是**用户自己填的**，而请求是**我们的服务端**发出的。不做校验的话，
 * 任何登录用户都能把我们的服务器当成跳板去打内网（MySQL、这台机器自己的 8081、云元数据
 * 169.254.169.254 等）。这是信任边界，不是可选项。
 *
 * <p>两道关：保存凭据时校验一次（早失败、给用户明确反馈），以及**每次真正出站前**在
 * {@link GuardedHttpTransport} 里再校验一次（模型客户端可能请求它自己拼出来的地址，
 * 只信任用户填的那个串是不够的）。
 *
 * <p><b>已知残余风险：DNS rebinding。</b>校验与连接之间存在解析窗口——先解析到公网通过校验、
 * 连接时再解析到内网。根治要在连接层按已解析的 IP 直连并自带 SNI/证书校验，而 JDK 的
 * HttpClient 做不到两全（直连 IP 就会用 IP 去校验证书）。这里选择了"不假装它被解决"：
 * 校验 + 禁止重定向 + 把这条写进报告。触发重评：多实例部署或对外网开放时上网络层出口策略。
 */
@Component
public class UpstreamGuard {

    public void assertAllowed(String baseUrl) {
        URI uri;
        try {
            uri = new URI(baseUrl);
        } catch (URISyntaxException exception) {
            throw rejected("地址无法解析");
        }
        if (!"https".equalsIgnoreCase(uri.getScheme())) {
            throw rejected("只接受 https 地址");
        }
        if (uri.getUserInfo() != null) {
            throw rejected("地址不得包含用户名或密码");
        }
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw rejected("地址缺少主机名");
        }

        InetAddress[] resolved;
        try {
            resolved = InetAddress.getAllByName(host);
        } catch (UnknownHostException exception) {
            throw rejected("主机名无法解析");
        }
        if (resolved.length == 0) {
            throw rejected("主机名没有解析到任何地址");
        }
        for (InetAddress address : resolved) {
            if (!isPublic(address)) {
                // 不把解析到的内网地址回给用户：那等于送他一个内网拓扑探测器
                throw rejected("解析到非公网地址");
            }
        }
    }

    private static boolean isPublic(InetAddress address) {
        if (address.isAnyLocalAddress()
                || address.isLoopbackAddress()
                || address.isLinkLocalAddress() // 含 169.254.169.254（云元数据）
                || address.isSiteLocalAddress()
                || address.isMulticastAddress()) {
            return false;
        }
        byte[] bytes = address.getAddress();
        if (bytes.length == 16) {
            // IPv4 映射地址（::ffff:a.b.c.d）要按内网规则判，否则 127.0.0.1 能借它绕过去
            if (isIpv4Mapped(bytes)) {
                return isPublicIpv4(bytes[12], bytes[13], bytes[14], bytes[15]);
            }
            // IPv6 唯一本地地址 fc00::/7（isSiteLocalAddress 只覆盖已废弃的 fec0::/10）
            if ((bytes[0] & 0xfe) == 0xfc) {
                return false;
            }
        }
        if (bytes.length == 4) {
            return isPublicIpv4(bytes[0], bytes[1], bytes[2], bytes[3]);
        }
        return true;
    }

    private static boolean isIpv4Mapped(byte[] bytes) {
        for (int index = 0; index < 10; index++) {
            if (bytes[index] != 0) {
                return false;
            }
        }
        return (bytes[10] & 0xff) == 0xff && (bytes[11] & 0xff) == 0xff;
    }

    private static boolean isPublicIpv4(byte a, byte b, byte c, byte d) {
        int first = a & 0xff;
        int second = b & 0xff;
        // 0.0.0.0/8、10/8、127/8、169.254/16、172.16/12、192.168/16、100.64/10（运营商级 NAT）
        if (first == 0 || first == 10 || first == 127) {
            return false;
        }
        if (first == 169 && second == 254) {
            return false;
        }
        if (first == 172 && second >= 16 && second <= 31) {
            return false;
        }
        if (first == 192 && second == 168) {
            return false;
        }
        if (first == 100 && second >= 64 && second <= 127) {
            return false;
        }
        return true;
    }

    private static BizException rejected(String reason) {
        return new BizException(ErrorCode.INVALID_ARGUMENT,
                "baseUrl 不被允许：" + reason + "（只接受 https 且解析到公网地址）");
    }
}
