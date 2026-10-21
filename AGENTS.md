# 项目协作说明

本项目使用已安装的 `project-lifecycle` Skill 管理需要持久追踪、多人协作或有明显风险的工作。

## 入口规则

- 低风险、边界清楚的单文件改动：确认目标 -> 修改 -> 窄验证，不创建 `WORK-*`。
- 重要工作开始或恢复时，先读取适用入口规则、`.agent/rules/always.md`、状态结果和当前工件；多仓库先从外层 `PROJECT-INDEX.md` 定位真实项目。
- 需要定位模块、历史变更或理解材料时先读 `.agent/INDEX.md`；它只做导航，不替代状态检查。
- 只有受管理需求创建 `WORK-*`；阶段批准、澄清、漂移、验证和完成语义统一遵循 Skill 的 `references/workflow.md`。
- 未完成硬依赖阻断实现和验收；重大范围、接口、数据、安全、部署或架构变化回到最早受影响工件。
- 规则未确认前不得进行实现、部署、迁移或数据变更；验证结果必须据实记录。
- 每次实际修改后追加 `.agent/history/updates.md`，记录变更、决策、依据、验证和本地提交边界；远端 `push` 必须得到用户明确授权。

## 节奏

细节、实测数字与踩过的坑统一记在 `.agent/notes/dev-workflow.md`，本节只留规则。

- **提交**：每完成一轮能独立验证的工作就提交一次，按切片分笔，只提交本次范围的文件。本地提交是检查点，`push` 仍须单独授权。
- **测试**：内环只跑受影响的用例，外环只跑**改动那一端**的 e2e，按端跑（`pnpm --filter @paideia/app test:e2e`、`--filter @paideia/admin test:e2e`、`--filter @paideia/ui-kit test:e2e`）。三端全量 `pnpm test:e2e:all` 只在改动跨端（动了共享包）时跑；桌面产物只在要更新桌面安装包时打。**不要用 `pnpm -r build`**——它连桌面壳一起打，近 20 分钟。
- **测试库是唯一入口**：任何碰数据库的验证都走 `scripts/test-db.sh`（或它包的 e2e），**不要直接 `mvn -B test` / `pnpm test:e2e` 连共用的 `paideia_test`**。这台机器上可能有别的会话同时在跑，共用库会让两轮测试互相删数据——单类跑全绿、全量跑红那种红**不是产品缺陷**，也不能当证据。脚本每次分配独占的 `paideia_run_<时间>_<pid>` 库，成功后自动删；失败时保留库名并打印出来。
- **并发会话各占一个工作区**：多个会话同时改这个仓库时，除主树 `F:\Paideia` 外，每个会话用 `git worktree add .worktrees/<名字> -b <分支>` 开自己的树（目录已忽略），互不踩 `target/`、`node_modules` 与工作区状态。主树保持在 `master`，只用于 review、落库和部署。并行 e2e 还要各挑一组端口：`PAIDEIA_BACKEND_PORT` / `PAIDEIA_APP_PORT` / `PAIDEIA_ADMIN_PORT` 都可覆写，否则 `reuseExistingServer` 会静默复用别人的服务、打到别人的库上。
- **工作树合并后必须删**，没有任何机制会自动回收：`git worktree remove .worktrees/<名字>` + `git branch -d <分支>`（合并后 `-d` 安全，未合并会拒删）。**实测占用**：只 checkout 约 5MB（`.git` 是共享的，不复制历史）；跑过后端测试 +100MB；跑过 `pnpm install` +814MB——一个真正用起来的工作树接近 1GB。不是"内存"是磁盘。
- **迁移与种子的分工**：生产迁移只建结构（可以插配置类常量行，但**插业务数据时要显式写 id 且 ≥ 900**）；测试/开发数据一律放 `db/devdata`，绝不放 `db/migration`。`MigrationSeedOverlapTest` 会把"生产迁移往种子表插行却不给 id"变成构建失败——这类 bug 只在零数据新库上出现，生产库和开发库永远看不出来。
- **迁移文件名用 14 位时间戳**：`V20261021103045__描述.sql`，不要用 `V25__` 这种递增号。多个会话并行时递增号是**被争抢的共享命名空间**：实测两个分支都写 `V25` 会让应用直接起不来（`Found more than one migration with version 25`），不是某个迁移失败而是整个上下文中止。时间戳把这个概率降到"同一秒"，`MigrationVersionFormatTest` 会同时挡住纯数字号与同秒重复。**它治不了**"两个会话改同一张表的不同列、版本号不冲突但语义冲突"——那只能靠合并前协调：**动 schema 前先 `git log --all --oneline -- <迁移目录>` 看有没有人在改同一张表**。
- **部署**：一条命令 `powershell -File scripts/release.ps1`（纯后端改动约 75 秒）。前端**按需重建**：`frontend/` 自上次发版以来没改动就自动跳过那 80 秒，要强制重建用 `-WithWeb`、强制只发后端用 `-SkipWeb`。部署形态、端口与服务器约定见 `.agent/rules/always.md` 的「部署」，耗时的实测拆解见 `.agent/notes/dev-workflow.md` 的「发版耗时」。本项目没有"发布"这个节点，改完随手部署即可，不需要攒批次。

## 保密规则（公开仓库，最重要）

本仓库是**公开仓库**：任何提交都会立即公开。凭据一旦进入 Git 历史，删除文件也无法补救（历史、fork、缓存仍可检索），因此只轮换凭据才算处理完。

- **禁止**将凭据、密钥、令牌、密码、私钥、数据库连接串、服务器地址写入任何被 Git 跟踪的文件：源码、配置、文档、`.agent/` 工件、`updates.md` 更新历史、提交信息、Issue 与 PR 正文。
- 本地敏感配置放在**被 `.gitignore` 忽略**的文件中（`.env.local`、`application-local.yml`、`.secrets/` 等）；仓库内只提交占位示例（如 `.env.example`）。
- 引用密钥时只写环境变量名，不写值。
- 服务器访问优先使用 SSH 密钥与普通用户，不用 root 密码；私钥文件不进仓库。
- 提交前检查暂存内容不含密钥；若已误提交，立即轮换凭据并清理历史，只删文件不够。
- 建议在 GitHub 仓库设置中启用 **Secret scanning** 与 **Push protection**（公开仓库免费），由平台在推送时拦截，无需自建钩子。

若未发现 `$project-lifecycle`，请先说明 Skill 未安装。
