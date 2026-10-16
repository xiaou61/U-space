package io.github.xiaou61.ai.internal.web;

import io.github.xiaou61.ai.internal.credential.CredentialService;
import io.github.xiaou61.platform.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 服务的状态端点（原"模型凭据端点"）。
 *
 * <p><b>WORK-009 起只读</b>：上游凭据由平台在部署配置里持有，用户不再配置 Key，所以保存（PUT）与
 * 删除（DELETE）两条写路径已下线。留一个 GET 是因为界面仍需要知道"平台配没配"——没配时该把
 * AI 入口做成不可用，而不是让用户点了才收到一个错误。
 *
 * <p>只对**已登录用户**开放，不要求任何角色：这是"服务可用不可用"的公共信息，不是管理操作。
 *
 * <p>响应里**没有任何 Key 片段**（连末四位也不给）：那是平台的 Key，不是用户的。
 */
@RestController
@RequestMapping("/api/v1/ai/credentials")
class AiCredentialController {

    private final CredentialService credentials;

    AiCredentialController(CredentialService credentials) {
        this.credentials = credentials;
    }

    @GetMapping
    ApiResponse<CredentialService.CredentialView> get() {
        // 仍要求登录：这是产品内的状态，不是公开探针
        AiRequester.require();
        return ApiResponse.ok(credentials.view());
    }
}
