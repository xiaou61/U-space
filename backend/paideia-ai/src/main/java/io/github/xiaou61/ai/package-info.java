/**
 * AI 精简模块的对外契约。
 *
 * <p>这一层只放"跨模块会被用到"的类型：一个端口（{@link io.github.xiaou61.ai.EntrySummarizer}）
 * 与它的值对象。**框架类型（AgentScope）不出现在这里**——这是 D-09 定下的纪律，
 * 效果是换模型实现或升级框架都不动业务代码。
 *
 * <p>内部实现全在 {@code internal} 子包：凭据的加密与存储、SSRF 校验、调用与缓存。
 * 本模块依赖 {@code paideia-knowledge} 只为一个方法（按 id 取已发布条目的正文）。
 */
package io.github.xiaou61.ai;
