---
artifact: tasks
work_id: WORK-027
work: 每日技术速递
status: draft
workflow: full
depends_on: [design]
related_to: [WORK-004, WORK-006, WORK-009, WORK-010, WORK-022, WORK-025]
created: 2026-10-20
updated: 2026-10-20
---

# 任务 · 每日技术速递

**排序原则**：先让**数据能进库**（T-01..T-05，纯后端、可单测、不碰界面），再让**一次 AI 调用可验证**（T-06），最后才做界面（T-07..T-11）。前面任何一环跑不通，后面的界面全是空壳。

**每个切片都可独立验证**：每完成一个任务就能跑一条命令看到结果，不需要等全部做完。

**全局约束**：不新增 Maven 依赖（RSS 用 JDK `javax.xml`，JSON 用已有 Jackson）；默认关闭（`enabled: false`）；不改 WORK-025 的任何代码；不动 `paideia` 服务以外的部署配置；**token 与任何凭据只进被忽略的本地文件**。

## 前置

**T-00 必须先满足**：WORK-025 的前端入口已完成。硬依赖未解除前，本项任何任务不得开工（`validate` 会拦）。

## 任务

### TASK-001 | pending | 迁移与数据模型

新建 `V25__create_digest_tables.sql`（**编号按当时实际最大值取，不许写死**）：`digest_items` 与 `digest_weeklies` 两张表，字段与索引见 design.md。`created_at` 由应用写入 UTC，**不用 `DEFAULT CURRENT_TIMESTAMP`**。

**验收**：`scripts/test-db.sh` 跑一次，`MigrationChecksumTest` 通过；`SHOW CREATE TABLE digest_items` 里能看到 `uk_digest_items_fingerprint` 与 `idx_digest_items_edition`。

### TASK-002 | pending | 指纹与 URL 规范化

`DigestFingerprint`：剥掉 `utm_*`、`ref`、`spm`、`from`、`fbclid`、`gclid` 查询参数，去掉末尾 `/`，小写化 host，取 SHA-256。

**验收**：单测覆盖——同一文章带 6 种不同追踪参数的 URL 得到同一个指纹；不同文章得到不同指纹；无查询参数的 URL 原样通过。

### TASK-003 | pending | RSS 源（五个源共用一个实现）

`RssDigestSource`：用 JDK `javax.xml.parsers` 解析 RSS 2.0 / Atom，取 `title`/`link`/`pubDate`/`description`（截断 200 字作 `summaryHint`）。**单个条目的 `pubDate` 坏掉只跳过该条，不整批失败。**

**验收**：单测喂三份夹具——① 正常 RSS ② 含一条坏 `pubDate` 的 RSS（断言好条目仍在）③ Atom 格式。真实联调一次：`qbitai` 与 `ithome` 各抓到非零条。（research.md 已实测两个源可访问）

### TASK-004 | pending | GitHub 与 HN 两个源

`GithubTrendingSource`：**有 token 走 Search API**（`q=created:>=<7天前> stars:>100&sort=stars`），**无 token 走页面解析**（先定位 `<h2>` 块再取 href —— 直接匹配 `href="/([^"]+)"` 会撞到赞助商链接），解析失败退回 Search API。
`HackerNewsSource`：Algolia `search?tags=front_page&hitsPerPage=10`。

**验收**：单测喂一份改坏的 HTML，断言返回空列表且**触发兜底**（用桩记录调用顺序）；真实联调两个源各抓到非零条。断言 GitHub 条目的 `heat` 来自当日新增 star。

### TASK-005 | pending | 编排服务与入库

`DigestCollectionService.runOnce()`：抓（单源 try/catch）→ 批内去重 → 查库剔除已存在（**只查 `edition_date` 在 [今天-1, 今天+1] 的窗口**）→ 截断 → 调 AI → 入库。

**验收**：集成测试用一个假 `DigestSource` 喂三条（其中一条与库里已有相同指纹）——断言只新增两条。**反向验证：把窗口查询改成全表，测试仍应通过（证明行为不变）；把唯一约束去掉后重复插入应被测试抓到。**

### TASK-006 | pending | 一次 AI 调用与降级

`DigestSummarizer` + `DigestPrompt`：一次调用，输出 `{"items":[{fingerprint,title,summary}]}`，**按指纹回填，不按顺序**。只接受输入集合内的指纹。

**验收**：单测三种响应——① 正常全返回 ② 少返回两条（断言其余正常、缺的标 `degraded`）③ 返回一个不在输入的指纹（断言被丢弃、不写垃圾行）。**AI 抛异常时整批按 `degraded` 入库，不中断**（AC-005 的单元级证据）。

### TASK-007 | pending | 调度、配置与遥测

`DigestProperties` + `DigestConfiguration`（`@ConditionalOnProperty`）+ `DigestJob`（`fixedDelay=24h`, `initialDelay=5min`，自己接住异常）+ 新增 `RunKind.DIGEST`。`application.yml` 加 `paideia.ai.digest` 段，**默认 `enabled: false`**。

**验收**：`enabled=false` 时容器里没有 `DigestJob` Bean（启动断言）；`enabled=true` 时有一条；`RunKind.DIGEST` 出现在 `ai_runs` 的统计筛选里。

### TASK-008 | pending | 学习者端读接口

`DigestController`：`GET /api/v1/digest?date=`、`/digest/dates`、`/digest/weekly`。**要登录**（与知识库同门禁）。

**验收**：集成测试——未登录 401；已登录返回按 `group_key` 分组的结构；不传 date 返回今天；`date` 格式非法返回 400。

### TASK-009 | pending | 学习者端页面

`DigestPage.tsx` + 路由 `/digest` + `LearnerHeader` 的 `NAV` 加一项 + `HomePage` 的 `QUICK_LINKS` 加一张卡。

**验收**：e2e 从首页与导航都能进；默认显示当天；能切到前一天；`degraded` 条目显示原文标题而不是空摘要。

### TASK-010 | pending | 周报

`DigestWeeklyService`（聚合上周 → 一次 AI 调用 → upsert 到 `digest_weeklies`）+ `DigestWeeklyJob`（`fixedDelay=7d`）+ `RunKind.DIGEST_WEEKLY` + 页面「周报」页签。

**验收**：集成测试——同周跑两次只有一行（AC-016）；周报生成抛异常时日报数据不受影响、接口返回 null 而不是 500（AC-013）。

### TASK-011 | pending | 管理端删除

`DigestAdminController`：`GET /api/v1/admin/digest?date=`、`DELETE /api/v1/admin/digest/{id}`。**路径规则里必须写 `hasRole('ADMIN')` 且顺序在免鉴权规则之前**（WORK-004 的教训）。管理端加一页（列表 + 删除）。

**验收**：集成测试——非管理员 403、管理员可删、删后学习者端读不到（AC-010）。

### TASK-012 | pending | 按身份分组与转笔记

`identity` 配置加 `digestGroups`；`DigestPage` 按它排序与过滤；`NoteNewPage` 读 query 参数预填标题与正文；卡片加「存成笔记」按钮。

**验收**：单测——`configFor()` 对不同身份返回不同 `digestGroups`；改 `digestGroups` 后页面分组顺序随之变化（AC-014）。e2e——点「存成笔记」跳到 `/notes/new` 且标题与正文已预填，保存后出现在笔记列表（AC-015）。

### TASK-013 | pending | 实跑与收口

`enabled: true` 实跑一次完整日报 + 一次周报，核对真实抓取条数、AI 用量与页面呈现。把实测数字写进 `testing/report.md`。

**验收**：`ai_runs` 里当日 `kind=digest` **恰好 1 条**（AC-008）；页面四组都有内容；抽查 5 条确认摘要是我们自己写的、有来源链接（AC-007）；`project-lifecycle.ps1 validate` 无错误。

## 依赖关系

- T-01 → T-02..T-06 全部
- T-02、T-03、T-04 → T-05
- T-05、T-06 → T-07
- T-07 → T-08 → T-09
- T-09 → T-12
- T-07 → T-10（周报复用编排与 AI 端口）
- T-08 → T-11（管理端读同一批表）
- 全部 → T-13

## 评审焦点

规格暗示但任务未直接覆盖的失败模式，逐条列在 design.md 的「评审焦点」表里。三条最容易被漏的：

1. **AI 少返回条目**（T-06 的第二个夹具）——用顺序对齐的实现会静默错位，界面看起来"摘要串了"。
2. **空候选池**（所有源都失败）——必须不调 AI、不写行、不报错（T-05 的边界）。
3. **管理端删除后重跑又抓回来**（T-11）——这是已知行为，**要写进文档而不是"修"它**；修它等于引入黑名单，那是另一个功能。
