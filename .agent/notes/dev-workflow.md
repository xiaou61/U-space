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
