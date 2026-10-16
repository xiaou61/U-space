# 开发节奏：实测数字、陷阱与理由

本文是 `AGENTS.md`「节奏」一节的展开。规则留在那里，这里放**为什么**、**实测多少**、**踩过什么坑**——需要判断时读这一篇。

## 三环测试节奏

**核心事实：贵的不是用例，是每次运行的固定开销。** 后端集成测试要连远程 MySQL；纯函数测试跑完是 0.1 秒。所以内环要靠"不启容器、不进网络"。

| 环 | 什么时候 | 跑什么 | 目标 |
| --- | --- | --- | --- |
| 内环 | 每改完一处 | `mvn -B -pl <模块> -am test -Dtest=<类>`；前端 `pnpm --filter <包> test` | < 30 秒 |
| 外环 | 一个切片收口 | 后端全量 + 前端 typecheck/test + **改动那一端**的 e2e | < 5 分钟 |
| 最外环 | 发布前 | 四个应用构建 + 桌面产物 + 全部 e2e | < 15 分钟 |

## 实测基线（2026-10-10）

| 动作 | 实测 |
| --- | --- |
| 内环：纯函数测试（16 条） | **7 秒** |
| 全量后端 `mvn -B verify`（124 条） | **92–102 秒** |
| 三端 e2e `pnpm test:e2e:all`（23 条，并行共用一个后端） | **24 秒**（逐端串行是 44 秒） |
| 桌面打包 `pnpm -w build:desktop` | **59 秒**（Electron 已缓存；首次下载约 20 分钟） |

## 两条反直觉结论（别再试一遍）

1. **集成测试全程只有 1 个 Spring 上下文**。用 `logging.level.org.springframework.test.context.cache: DEBUG` 确认过：`size = 1, missCount = 1`。所以"合并上下文"没有可省的空间，那 11–21 秒/类是**测试本身的活**。
2. **给集成测试加并行度是负收益**：`-DforkCount=2 -DreuseForks=true` 实测 **63 秒**，比默认 **60 秒**还慢——每个 fork 有自己的上下文缓存，重建代价吃掉并行收益。不要为了好看加并行度。

**真正的瓶颈**是每个 SQL 都要过 SSH 隧道到远程 MySQL。唯一能大幅砍掉的办法是把测试库放本地（当前是"直连服务器 MySQL、不起本地实例"的既有取舍）。

## 在线上跑 e2e（2026-10-12 起，改完直接部署再验）

管理端的 e2e **可以不用本地任何东西**：设了 `E2E_BASE_URL` 就跳过 `webServer`，直接打已部署实例。界面类改动走这条最快——不用本地后端、不用本地 dev server、也不用那条 3307 隧道。

```
MSYS_NO_PATHCONV=1 E2E_BASE_URL=http://<服务器>:8081 E2E_ADMIN_PATH=/admin/ \
  E2E_ADMIN_IDENTIFIER=… E2E_ADMIN_PASSWORD=… \
  E2E_LEARNER_IDENTIFIER=… E2E_LEARNER_PASSWORD=… \
  pnpm --filter @paideia/admin test:e2e
```

- 线上实测 **7 passed / 14.7 秒**（本地那条路 23–27 秒，且依赖隧道）。
- `E2E_ADMIN_PATH` 必须给：部署形态是"一个进程同时供 API 与两个前端"，**两个前端各占一个地址**——学习者端在 `/`、管理端在 `/admin/`（`/admin` 与 `/admin/` 都由 view controller 转发到管理端的 index.html）。
- **账号必须给**：种子账号（`seed-admin`/`seed-learner`）不在线上——口令写在公开仓库里，放到公网可达的实例上等于开放管理权限。线上是 `admin` 与 `e2e-learner`（后者是为 e2e 建的普通学习者），口令见被忽略的 `backend/config/application-local.yml`。
- **`MSYS_NO_PATHCONV=1` 不能省**：Git Bash 会把 `/admin/index.html` 这类值改写成 Windows 路径，测试会全红、且报错指向"元素找不到"，极易误判成界面坏了。
- 这条路径的两个边界：① 它**真的写线上库**（知识库那条用例自己建、自己删）；② 注册/邮箱验证码那几条**只能在本机跑**——要靠内存信箱读验证码，线上是真实 SMTP。学习者端的 e2e 因此仍走本地那条。

## 踩过的坑

- **不要用 `pnpm -r build`**：`apps/desktop` 的 `build` 会跑 `electron-builder`（下载 Electron 再打包），一轮接近 20 分钟。构建用 `pnpm --filter @paideia/<app> build` 或 `pnpm -r --filter '!@paideia/desktop' build`。
- **不要用 `@SpringBootTest(properties = …)` 覆盖测试配置**：配置键不同 = 又一个 Spring 上下文。需要的数据源统一列在 `paideia-app/src/test/resources/application-test.yml`；测试装配统放在 `TestProfileConfiguration`（`@Profile("test")`，会被组件扫描）。
- **删掉一个资源文件（如迁移 SQL）后必须 `mvn clean`**：Maven 不会从 `target/classes` 删掉已移除的资源，它会继续被打进 jar。2026-10-10 把两个临时探针迁移带上了部署。
- **`pnpm -r test:e2e` 会把桌面端也跑起来**，而它的 e2e 需要已打包的 Electron 产物、本机跑不起来，整条命令会失败。用 `pnpm test:e2e:all`（已排除桌面端）。
- **管道会吃掉退出码**：`cmd | head` 的退出码是 `head` 的；判断成败不要走管道，或显式取 `${PIPESTATUS[0]}`。
- **写断言前先读数据**：写 e2e 断言时凭印象假设种子内容，连错两次（条目只有一个标题所以没有目录、种子里的已发布条目各占一个分类所以没有上下篇），每次都要完整跑一轮 e2e 才发现。读一次文件比跑两轮便宜得多。

## 提交节奏的理由

本仓库可能同时有多个会话在同一工作区改动，攒着不提交会让"哪些改动属于谁"变得不可判定，也让误操作的回退范围变大。分笔提交把回退粒度压到一轮。

## 附件存储：对象存储（WORK-007，2026-10-15 起）

**形态**：附件走自建 S3 兼容服务 **RustFS 1.0.1**，与后端**同机**（服务器没有 Docker）。

| 项 | 位置/取值 |
| --- | --- |
| systemd 单元 | `rustfs`（enabled + active，`Restart=always`） |
| 二进制 | `/usr/local/bin/rustfs`（来自官方 `.deb`，**解包而不是 `dpkg -i`**） |
| 数据 | `/var/lib/rustfs/data`（桶 `paideia-attachments`） |
| 配置 | `/etc/rustfs/rustfs.env`（**600，仅 root**，凭据随机生成；`*.bak-*` 是改动前的备份） |
| 监听 | **`0.0.0.0:9000`（S3 API，刻意对公网开放）**；控制台**已关闭**（`RUSTFS_CONSOLE_ENABLE=false`） |
| 应用侧 | `/opt/paideia-app/application.yml` 的 `paideia.knowledge.storage: s3` + `s3.*`（endpoint 用**公网地址**，因为浏览器要直连它） |
| 应用日志 | 启动时打一行 `附件存储：对象存储 <endpoint> 桶 <bucket>`（**不含凭据**） |

**为什么对象存储要对公网开放**：读路径是"后端 302 → 预签名 URL → 浏览器直连对象存储"，所以浏览器必须够得着它。加固是配套的：控制台关闭、桶私有（无匿名策略，任何读取都要有效签名）、凭据随机且只在 600 文件里、签名有效期 7 天。

**为什么没开 9000 以外的口**：这台机器上非 80/443/8080 的端口本来就公网可达（云安全组只管那三个），所以"开放"和"只开这一个口"是同一件事。

### 切换与回滚

```yaml
# /opt/paideia-app/application.yml
paideia:
  knowledge:
    storage: s3        # 改回 local 即回退
```

改完 `systemctl restart paideia` 生效（**不做运行时不重启热切**：存储位置会变，热切需要额外的一致性设计）。

**⚠️ 回滚是有损的**：切回 `local` 之后，期间上传到对象存储的附件**会 404**（字节在对象存储里，本地目录没有）。切换前先确认这个损失面可接受——2026-10-15 首次切换时线上附件为 **0 条**，所以那次损失面为零。

### 新环境要手动建桶（应用**不自动建桶**）

故意不自动建：配错桶名时自动建会让"附件看起来全都消失了"（其实进了另一个桶），极难排查。新环境先建一次：

```bash
python3 -c "import boto3; boto3.client('s3', endpoint_url='http://<host>:9000', aws_access_key_id='<AK>', aws_secret_access_key='<SK>', region_name='us-east-1').create_bucket(Bucket='paideia-attachments')"
```

（或者用 `mc mb`；注意 `dl.min.io` 已 410，mc 要从 GitHub 下。）

### 两个参数与它们的理由

- `presign-ttl: 7d`——预签名 URL 的有效期。
- `redirect-cache: 1h`——**302 跳转本身**允许被浏览器缓存多久，必须**小于** presign-ttl（装配期会拦住反过来的配置）。
  这条参数是为了补一个副作用：签名 URL 每次都不同，而浏览器缓存按完整 URL 索引，所以"每次都换签名"会把本项之前靠"id 不变"换来的一年浏览器缓存打到 0。让 302 自己缓存一小时，同一小时内的重复加载直接用缓存里的跳转，既不回后端也不回对象存储。

### 排障

```bash
systemctl status rustfs; journalctl -u rustfs -n 50          # 对象存储侧
journalctl -u paideia | grep 附件存储                          # 应用选了哪个后端
curl -i http://<host>:8081/api/v1/knowledge/files/<uuid>      # 期望 302 + Location 指向 :9000
curl -sL -o /tmp/a.png http://<host>:8081/api/v1/knowledge/files/<uuid>   # 跟随后应拿到字节
```

**跑集成/契约测试需要两条隧道**：3307（MySQL）与 9000（对象存储）。契约测试的 S3 那条以环境变量为开关（`PAIDEIA_KNOWLEDGE_S3_ENDPOINT`），没有隧道时**跳过而不是失败**。

## AI 计费与平台凭据（WORK-009，2026-10-16 起）

**凭据来源变了**：AI 的上游 Key 由**平台**持有、放**部署配置**（`paideia.ai.billing.platform-credential.{base-url,model,api-key}`，环境变量占位），**不入库**。用户不再配置 Key，`/api/v1/ai/credentials` 只剩 GET（返回"配没配"，**连末四位也不给**）。随之删掉的是用户加密存 Key 的那张表 `ai_model_credentials` 与 `CredentialCipher`——**没有秘密要落库了**，`paideia.ai.master-key` 也不用再配（老配置里还留着它无害，只是没人读）。

**积分**：每用户一个余额（`ai_credit_accounts`）+ 一条**只增不改**的流水（`ai_credit_ledger`，自带 model/input/output，**不靠 join** 别的表）。流程是**先预留、后按实际用量结算**：

- 预留额 = `max-credits-per-call × reserve-factor`（默认 5000 × 0.2 = **1000**）。余额不够时**一个上游请求都不发**，返回 HTTP **402**（业务码 40200，界面据此提示"积分不足，当前没有自动充值，请联系管理员"）。
- 结算按价目表折算并**截到单次上限**；实际 < 预留就退还差额。**拿不到用量就整笔退**——没有依据就不扣。
- **本期没有充值**：积分唯一入口是管理端 `POST /api/v1/ai/admin/credits/{userId}/adjust`（带 delta 与 note，操作人与原因进流水）。

**价目表改法**（改配置即生效，不用重新构建）：

```yaml
paideia:
  ai:
    billing:
      pricing:
        deepseek-flash:
          input: 100     # 每千 input token 的积分
          output: 200    # 每千 output token 的积分
```

**没配到价目就按 0 扣**（宁可漏扣、不误扣）：审计页上"有用量但 0 扣减"一眼看得出来。

**回滚档位一**：`paideia.ai.billing.enabled=false` → 回到"平台凭据 + 不计量"，用户无感，已生成的流水不受影响。**不写**整表删除的回滚脚本（流水是审计证据）。

**两条顺序敏感的约束**（写反了**不报错**，静默失效）：

1. `/api/v1/ai/admin/**` 的 `hasRole("ADMIN")` **必须排在 `/api/v1/ai/**` 之前**——顺序写反只会在某天表现为"任何已登录的学习者都能看别人的用量、给自己发积分"。
2. 新迁移接着仓库当前最大号（本项是 **V11**）。

**已知缺口**（登记，不在本项解决）：

1. **进程在"预留"之后、"结算"之前被杀** → 留下一笔未结算的预留，余额被多占。本项只保证**不超扣**；多占要人工按流水核对（缺 `REFUND` 即可定位）。不引入定时任务。
2. **缓存折扣拿不到**：`TokenUsage` 只有 input/output/total，上游把缓存命中单独计价时我们按全价扣，计费偏高。
3. 回收不足时（预留估少了）那笔差额**不补扣**，只写进流水备注留痕。

**跑集成测试**：需要 DB 隧道 + `PAIDEIA_TEST_DB_PASSWORD`。**没设口令时 `paideia-app` 整模块跳过（118 条全 Skipped）而 BUILD 仍 SUCCESS**——看结果只看 `Skipped` 那一列。隧道本身要服务器的 SSH 口令，`quick-server` 的包装器只支持远程执行、**不支持本地端口转发**，所以非交互环境下拿不到这条隧道。


## AI 助手面板与会话历史（WORK-011，2026-10-16 起）

**面板形态**：**可随处拖动的浮动对话框**（2026-10-16 用户看过"右侧停靠"版后改向——他要"随心所欲拖动、任意区域、小小的对话框、不要固定到某个界面"，并反馈过入口"不太明显"）。做法：`fixed` + 位置/尺寸都存本地（键 `paideia:assistant-panel`，值 `{open,x,y,w,h}`）；**拖标题栏移动**、**拖右下角握把缩放**（握把键盘左右箭头也能改宽）；默认右下角、384×560，范围宽 320–720、高 360–900；**始终夹回可视区**（至少留 16px 边缘可见，窗口缩小也会夹回来）；收起后是一个**显眼的药丸按钮**（图标 + 「AI 助手」），不是小圆点。

**没有"内容让位"这回事**：浮动即覆盖，所以不设任何根 CSS 变量，`AppShell` 也没有为它留 padding（停靠版的 `--assistant-panel-w` 那一处已撤回）。

**当前会话存本地**（键 `paideia:assistant-conversation`）。**三种状态必须分清**，它们都表现为"没有当前会话"：

| 值 | 含义 | 打开面板时 |
| --- | --- | --- |
| 键不存在 | 从没选过 | 恢复**最近一条**（有当前条目时优先绑在该条目上的那条） |
| 空串 `''` | 刚点了「新开对话」 | 保持空白，**不要再**去恢复旧的 |
| `'<id>'` | 选过某条 | 恢复这一条 |

少了这个区分，"点了新开 → 收面板 → 再打开"就会把刚丢开的那条又拉回来。

**换条目不再清空会话**：该条目有绑定的会话就切过去，没有就**保持当前这一段**；只有点「新开对话」才清。（旧实现一换条目就丢，那是用户最直接的不满。）

**读历史**：`GET /api/v1/ai/conversations/{id}/messages`（WORK-011 新增）。归属校验与其它会话端点**同一条 404**；条数上限取库里保留数（200）；返回旧 → 新，含 `role`/`content`/`usage`/`createdAt`。思考过程不落库，所以历史里没有它。

**两个坑，都踩过**：

1. **`ai_messages.token_usage`（列）与 `tokenUsageJson`（字段）自动映射对不上** —— 下划线转驼峰得到 `tokenUsage`，而字段名多了 `Json`，MyBatis **静默给 null**。这一列在此之前**只被写过、从没被读过**（`history()` 只取 role/content），所以潜伏到 2026-10-16 才被 WORK-011 的读历史用例抓出来。**读它的 SELECT 必须显式 `token_usage AS tokenUsageJson`。**
2. **面板挂载时的"恢复上次会话"是异步的**（先列会话、再拉消息），用户可能在这几十毫秒里已经点了发送；恢复完成时若照旧写入，会把在飞的那一轮 **abort 掉**（实测报 `signal is aborted without reason`）。修法：**一旦用户动过（发送 / 新开 / 切换）就让恢复作废**。

**收起 = 卸载**：外壳收起时走的是另一条 `return` 分支，所以"卸载即 abort 上游"这条**仍然覆盖收起**。将来若改成"收起但保持挂载"，必须显式在收起时 abort，否则一次调用会继续计费到用户看不见的地方。
