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

