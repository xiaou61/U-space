package io.github.xiaou61.ai.internal.web;

import io.github.xiaou61.ai.internal.credential.CredentialService;
import io.github.xiaou61.platform.ApiResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 模型凭据端点。
 *
 * <p>只对**已登录用户**开放，不要求任何角色：这是每个用户自己的凭据与自己的额度，
 * 不是管理操作。安全配置里为此显式写了一条规则（避免将来被误读成漏配）。
 *
 * <p>响应里**永远没有 Key**：只有 {@code apiKeyLast4}，够用户确认"我配的是哪一把"，
 * 不够任何人拿它去调用。
 */
@RestController
@RequestMapping("/api/v1/ai/credentials")
class AiCredentialController {

    /** 入参：三样都必填。校验在服务层做（那里还能顺手做 SSRF 校验）。 */
    record CredentialRequest(String baseUrl, String model, String apiKey) {
    }

    private final CredentialService credentials;

    AiCredentialController(CredentialService credentials) {
        this.credentials = credentials;
    }

    @GetMapping
    ApiResponse<CredentialService.CredentialView> get() {
        return ApiResponse.ok(credentials.view(AiRequester.require()));
    }

    /** 保存（有则覆盖）。同一个用户只有一份凭据，换 Key 就是覆盖。 */
    @PutMapping
    ApiResponse<CredentialService.CredentialView> put(@RequestBody CredentialRequest request) {
        return ApiResponse.ok(credentials.save(AiRequester.require(),
                request.baseUrl(), request.model(), request.apiKey()));
    }

    @DeleteMapping
    ApiResponse<Void> delete() {
        credentials.delete(AiRequester.require());
        return ApiResponse.ok(null);
    }
}
