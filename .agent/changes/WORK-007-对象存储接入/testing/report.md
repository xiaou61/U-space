---
artifact: report
work_id: WORK-007
work: 对象存储接入
status: passed
evidence: required
verified_commit: ebda90c
verified_at: 2026-10-15
---

# 验收报告 · 对象存储接入

## 结论

**通过。** 附件存储已从"只有本地磁盘一个实现"变成**同一份构建、两个实现、改配置 + 重启即换**，生产已切到自建 RustFS，读路径走预签名直读（302），端到端在学习者端看到图片真实渲染。

两处如实记下的偏差：① **管理端"点按钮上传"那条界面链本轮没被自动化盯住**（e2e 未覆盖上传按钮，本轮界面上传那次交互断了，改用同一 HTTP 端点从 API 验的）；② 预签名会吃掉浏览器长缓存，参数（TTL 7 天 / 302 缓存 1 小时）是折中值，**建议按真实流量再看一眼**。

## 验收矩阵

| AC | 判定 | 证据 |
| --- | --- | --- |
| AC-1 可换性：同一批用例在两种实现下都不改测试代码地通过 | ✅ | `FileStorageContractTest` **2 运行 / 0 失败 / 0 跳过**（本地临时目录 + 连真机 RustFS）。同一份 `assertContract` 跑两遍，含预签名 URL 真取回 200 且字节一致 |
| AC-2 契约不变：正文里的 `/api/v1/knowledge/files/{id}` 形状不变，端点仍只放行 GET | ✅ | 线上 `GET /api/v1/knowledge/files/<uuid>` 返回 **302**；`SecurityConfiguration` 未改（仍 `HttpMethod.GET` + `permitAll`）；管理端 e2e 8 passed 未受影响 |
| AC-3 不可枚举：id 仍是 UUID 且被两个实现共同校验 | ✅ | `FileIds.isUuid` 单点共用；契约测试含"非 UUID 的 id 读必须为空、删必须 false"两条断言 |
| AC-4 元数据不搬家：`knowledge_files` 表与写入路径不变 | ✅ | 无迁移；`FileService.upload` 仍是"先校验→落盘→插表，插表失败删对象"；上传返回 id/url/type/size/name 形状未变 |
| AC-5 生产可用：线上附件可上传、可在学习者端渲染 | ✅ | 上传 420×150 测试图 → 条目 id=17 发布 → 学习者端 `img.complete=true, naturalWidth=420, naturalHeight=150`；`curl -L` 取回 2932 字节与原文件 `cmp` 一致；截图存证 |
| AC-6 回滚路径明确且写明有损 | ✅ | `dev-workflow.md` 写明"改回 local + 重启"与"S3 期间上传的附件会 404"；首次切换时线上附件 **0 条**（`knowledge_files` 0 行、正文 0 处引用、目录为空），损失面为零 |
| AC-7 暴露面受控 | ✅ | 外部实测：`9000 → 403`（可达且拒绝未签名请求）、`9001 → 000`（控制台关闭不可达）；桶私有、凭据 24/40 位随机且只在 `/etc/rustfs/rustfs.env`(600)；应用日志只打 endpoint 与桶名 |
| AC-8 配置错误 fail fast | ✅ | `FileStorageConfigurationTest` 5 条：默认→local、显式 local→local、s3→S3FileStorage、s3 缺凭据→上下文启动失败、`redirect-cache >= presign-ttl`→启动失败 |

## 反向验证（"这条要是坏了，哪个断言会红"）

| 被验行为 | 反着改一处会怎样 |
| --- | --- |
| 默认仍是本地实现 | 把 `matchIfMissing` 去掉 → `defaultsToLocalStorage` 红 |
| 直读 URL 真的可用 | 把 `presignGetUrl` 的 TTL 设成 0 → 契约测试里那次取回拿不到 200 |
| 删除语义与本地一致 | 去掉 `delete` 里的 head → "删不存在的对象返回 false"红 |
| 非 UUID 不可枚举 | 去掉 `FileIds.isUuid` 调用 → "非 UUID 读必须为空"红 |
| 302 缓存必须短于签名 | 把 `redirect-cache` 配成 2h → 装配测试红（已实测） |

## 实测数字（2026-10-15）

- RustFS **1.0.1**（GA 1.0.0 于 2026-09-16、补丁 10-03；**GA 仅约四周**是唯一成熟度顾虑）；`MemoryCurrent=64MB`。
- 后端 `mvn verify`：**BUILD SUCCESS**；`paideia-app` 116 运行 / 115 跳过（未给数据库口令时集成测试按设计跳过）。
- 上传→302→直读：`X-Amz-Expires=604800`（7 天）、`Cache-Control: max-age=3600, private`、取回 200 / 2932 字节一致。
- 用户可见面：学习者端条目里图片正常渲染（截图）。

## 未覆盖 / 待办

1. **管理端上传按钮的界面自动化**：e2e 没覆盖（本轮那次界面操作断了）。建议后续给 `admin-entry-upload` 补一条：点按钮 → 选文件 → 正文末尾出现引用 → 保存 → 学习者端能看到。
2. **预签名参数复核**：按真实流量看"302 缓存 1 小时"够不够、签名 7 天是否过长。
3. **备份**：对象存储数据在 `/var/lib/rustfs/data`，目前没有备份策略（本地目录时代也没有）——属 D-15 范畴。
