---
artifact: design
work_id: WORK-027
work: 每日技术速递
status: draft
workflow: full
depends_on: [proposal]
related_to: [WORK-004, WORK-006, WORK-009, WORK-010, WORK-022, WORK-025]
created: 2026-10-20
updated: 2026-10-20
---

# 设计 · 每日技术速递

## 数据模型

两张表。**不建第三张**——日报按 `edition_date` 分组从 `digest_items` 直接取，成刊状态由「当天有没有行」推出（I-03）。

### `digest_items`

```sql
CREATE TABLE digest_items (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  -- 归属哪天：按 UTC 日切，与 edition 查询的索引一致
  edition_date  DATE            NOT NULL,
  -- news | github | community | hackernews；决定前端分组
  group_key     VARCHAR(16)     NOT NULL,
  -- 源标识，如 qbitai / ithome / infoq / juejin / oschina / github-trending / hackernews
  source_key    VARCHAR(32)     NOT NULL,
  source_name   VARCHAR(64)     NOT NULL,
  title         VARCHAR(512)    NOT NULL,
  -- 我们自己写的一句话摘要；AI 失败时为 NULL（降级态）
  summary       VARCHAR(1024)   NULL,
  url           VARCHAR(1024)   NOT NULL,
  -- 规范化 URL 的 SHA-256，去重靠它
  fingerprint   CHAR(64)        NOT NULL,
  -- 源自身的信号：HN 点数、GitHub 当日新增 star；其它源为 NULL
  heat          INT             NULL,
  -- 一条副信息：语言、作者、评论数等，直接给人看
  meta          VARCHAR(255)    NULL,
  -- 原文发布时间（UTC 毫秒）；源不提供时为抓取时间
  published_at  BIGINT          NULL,
  -- ready | degraded（AI 没写成摘要）
  summary_state VARCHAR(16)     NOT NULL DEFAULT 'ready',
  created_at    DATETIME(3)     NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_digest_items_fingerprint (fingerprint),
  KEY idx_digest_items_edition (edition_date, group_key, heat)
) ENGINE = InnoDB;
```

**为什么 `fingerprint` 上是全局唯一**（与 WORK-025 的「同分类内判重」不同）：新闻的重复是**跨源**的（同一条被两个门户转），不是分类内的。全局唯一直接挡住。

**为什么 `published_at` 是 BIGINT 而不是 DATETIME**：源给的是 epoch 秒/毫秒，转来转去容易错时区。存原始 epoch，展示时由前端按用户本地时区格式化。

### `digest_weeklies`

```sql
CREATE TABLE digest_weeklies (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  -- 该周周一的 UTC 日期
  week_start   DATE         NOT NULL,
  body         MEDIUMTEXT   NOT NULL,
  -- 生成时用了多少条日报素材，便于回看质量
  item_count   INT          NOT NULL,
  created_at   DATETIME(3)  NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_digest_weeklies_week (week_start)
) ENGINE = InnoDB;
```

`week_start` 唯一：重复跑不产生第二条（AC-016）。重跑时用 `ON DUPLICATE KEY UPDATE` 覆盖。

**不建外键**：两张表与账号无关（内容是公共的），删除用户不涉及。

## 端口与类

```
paideia-ai/internal/digest/
  DigestSource.java          端口：fetch(窗口) -> List<FetchedItem>
  FetchedItem.java           record(title, url, summaryHint, heat, meta, publishedAt, sourceKey, sourceName)
  RssDigestSource.java       三个门户 RSS + 两个社区 RSS 共用一个实现，按配置区分
  GithubTrendingSource.java  页面解析 + Search API 两条路
  HackerNewsSource.java      Algolia
  DigestProperties.java      配置
  DigestConfiguration.java   条件装配（两个调度）
  DigestCollectionService.java  编排：抓 → 去重 → 一次 AI 压缩 → 入库
  DigestSummarizer.java      那一次 AI 调用
  DigestPrompt.java          提示词与解析
  DigestFingerprint.java     URL 规范化 + SHA-256
  DigestWeeklyService.java   周报聚合与一次 AI 调用
  DigestJob.java             日报调度
  DigestWeeklyJob.java       周报调度
  DigestMapper.java          两张表的 SQL
  web/DigestController.java  学习者端读接口
  web/DigestAdminController.java  管理端删除
```

**一个 `RssDigestSource` 服务五个源**（而不是五个类）：它们的行为完全一样（解析 RSS、取 title/link/pubDate），差别只在 URL 与显示名。五个类会有四份重复代码——这与 WORK-025「一源一类」不同，因为牛客是**结构化 API**、语义与 RSS 完全不同。

GitHub 与 HN 各一个类，因为它们的响应形态不同（HTML / JSON）。

## 编排流程

```
runOnce():
  1. 未启用 / 分类缺失 / 凭据缺失 → 跳过并返回原因
  2. 对每个启用的源：抓取（包 try/catch，单源失败只记日志）
  3. 合并 → 规范化 URL 指纹 → 批内去重
  4. 查库：已存在的指纹剔除（只查当天及前后一天，不查全表）
  5. 按上限截断（maxItemsPerRun）
  6. 一次 AI 调用：输入全部候选，输出 [{fingerprint, title, summary}]
  7. 逐条入库；AI 没返回的条目 summary_state='degraded'，summary=NULL
  8. 记 RunKind.DIGEST 的用量
```

**第 4 步的窗口**：不查全表，只看 `edition_date` 在 [今天-1, 今天+1] 的行。新闻不会隔一个月又重新出现；查全表会让每次运行扫一张不断增长的表。

**第 6 步的失败语义**：AI 调用抛异常 → **整个 run 不中断**，所有条目按 `degraded` 入库（AC-005）。这比"全部不入库"合理：标题 + 链接本身就有价值。

## AI 调用设计

一次调用、两件事（挑 + 写），所以输出必须是结构化 JSON：

```json
{"items": [
  {"fingerprint": "sha256...", "title": "中文标题", "summary": "一句话摘要"},
  ...
]}
```

**输入给模型的字段**：`fingerprint`（用于回填）、原始标题、源名、`heat`、`summaryHint`（RSS 的 description 或 HN 的标题上下文，**截断到 200 字**）、语言标记。

**提示词约束**（三段，都写死在代码里，与 `EssayExtractionPrompt` 同形态）：
1. 只根据给定信息写，信息不足就说「标题说了什么」，**不得编造事实**；
2. 标题要中文；原文是中文的保持原标题，不改写；
3. 输出条数**不得超过输入条数**，可以少（挑掉不重要的），但不能多。

**为什么用 `fingerprint` 回填而不是顺序对齐**：模型可能少返回条目，顺序也会变。用指纹匹配是唯一不会错位的做法。

**DeepSeek 限制**：`nativeStructuredOutput(false)` + 不用工具（与 `EssayExtractor` 同配置）；`maxTokens` 给足 4096，因为思考 token 会占额度。

## 周报

```
runWeekly():
  1. 取上周一至今的 digest_items，按 group_key 分组，每条只取 title
  2. 总量超过阈值（默认 150 条）时按天分段摘要两轮，否则一次
  3. 一次调用生成 markdown「本周技术主线」，300~500 字
  4. upsert 到 digest_weeklies（week_start 唯一）
```

**为什么允许两轮**：一周 6 天 × 约 50 条 = 300 条，输入可能到 3 万 token。仍在上下文内，但输出质量会下降。阈值可配，先用一次；超阈值才拆。

**周报失败不影响日报**：独立调度、独立 try/catch（AC-013）。

## 接口

学习者端（都要登录，与知识库同门禁）：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/v1/digest?date=YYYY-MM-DD` | 某日全部条目，按 `group_key` 分组返回；不传 date 取今天 |
| GET | `/api/v1/digest/dates?limit=30` | 有内容的日期列表（给日期选择器） |
| GET | `/api/v1/digest/weekly?week=YYYY-MM-DD` | 某周周报；不传取本周；无则返回 null |

管理端（`hasRole('ADMIN')`，**规则必须写在路径规则里且顺序在前**——WORK-004 的教训）：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/v1/admin/digest?date=` | 当日条目（含 `source_key`、`fingerprint`，便于排查） |
| DELETE | `/api/v1/admin/digest/{id}` | 删除单条 |

**删除是硬删**（不是软删）：这是从外面抓来的公开新闻，没有审计价值，也没有"恢复"需求。删掉之后同一天重跑**不会**再抓回来（指纹已不在库里 → 会重新入库），这是已知行为——**若需要永久屏蔽某个 URL，那要另做黑名单，一期不做**。

## 前端

`frontend/apps/app/src/pages/DigestPage.tsx`（新），路由 `/digest`。

结构：
- 顶部：日期选择（今天 / 前一天 / 日期列表）
- 两个页签：「日报」「周报」
- 日报：四组卡片，组内有条数徽标；每组内按 `heat` 降序（无 heat 的按发布时间）
- 每条卡片：中文标题、一句摘要、来源徽标 + heat 数字（有的话）、「存成笔记」按钮
- `summary_state='degraded'` 时摘要位置显示原文标题（灰字），不显示空

「存成笔记」= `<Link to={'/notes/new?title=…&content=…'}>`（I-16）。**需要给 `/notes/new` 加读取 query 参数预填**——这是本项对笔记页唯一的改动。

按身份分组（I-15）：`frontend/packages/identity/src/config.ts` 加一项 `digestGroups: readonly GroupKey[]`，`DigestPage` 按它排序与过滤。`job_seeker` 排「news, github, community, hackernews」；其余身份也一样（一期配置值相同，只是结构就位）。

导航：`LearnerHeader.tsx` 的 `NAV` 加一项 `{ key: 'digest', to: '/digest', label: '每日速递' }`，并加进 `identity` 的 `preferredNav` 类型联合。

首页：`HomePage.tsx` 的 `QUICK_LINKS` 加一张卡。

## 配置

```yaml
paideia:
  ai:
    digest:
      enabled: false              # 默认关闭，与 essay / decision 同纪律
      # GitHub token；未配置时退回匿名（限额 60/时）
      github-token: ${DIGEST_GITHUB_TOKEN:}
      # 单次运行最多入库条数（成本闸）
      max-items-per-run: 60
      # 每周最多取多少条日报素材做周报
      weekly-max-items: 150
      # 日期窗口：抓多久以内的内容（小时）
      lookback-hours: 30
      timeout-seconds: 20
      publish-directly: true      # D-07 内容直出
      rss:
        - key: qbitai
          name: 量子位
          url: https://www.qbitai.com/feed
          group: news
        - key: ithome
          name: IT之家
          url: https://www.ithome.com/rss/
          group: news
        - key: infoq
          name: InfoQ
          url: https://www.infoq.cn/feed
          group: news
        - key: juejin
          name: 掘金
          url: https://juejin.cn/rss
          group: community
        - key: oschina
          name: 开源中国
          url: https://www.oschina.net/news/rss
          group: community
```

**源清单在配置里而不是代码里**：加一个 RSS 源不该改代码。这是与 WORK-025 不同的一处——牛客是一个特殊 API，速递的 RSS 源是同质的。

## 与既有模块的关系

- **不新增 Maven 依赖**：RSS 解析用 JDK 的 `javax.xml` DOM/SAX（项目里没有 RSS 库，为五个源引一个依赖不值得）。JSON 用已有的 Jackson。
- **不依赖 `paideia-knowledge`**：速递不进知识库（I-02）。
- **不依赖 `paideia-note`**：转笔记是纯前端跳转（I-16）。
- **复用**：`GuardedHttpTransport`（AI 调用）、`CredentialService`（取平台凭据）、`RunRecorder`（用量）、`BizException`。

## 评审焦点（规格没覆盖、实现时容易错的地方）

| 失败模式 | 指到 |
| --- | --- |
| 同一 URL 带不同 `utm_*` 参数 → 两条 | `DigestFingerprint` 单测 + AC-002 |
| RSS `pubDate` 缺失或格式异常 → 整批解析失败 | `RssDigestSource` 单测：坏条目跳过、好条目保留 |
| GitHub 页面结构变化 → 解析返回空列表而非抛异常 | `GithubTrendingSource` 单测：喂一份改坏的 HTML，断言空列表 + 走兜底 |
| AI 少返回条目 → 顺序对齐错位 | 指纹回填（设计已定）+ 单测：喂少两条的 JSON |
| AI 返回不在输入里的 fingerprint → 写入垃圾行 | 只接受输入集合内的指纹，其余丢弃 |
| 并发两个实例同时跑 → 重复入库 | `uk_digest_items_fingerprint` 唯一约束挡住，捕获重复键异常按"已存在"处理 |
| 管理端删除后同一天重跑又抓回来 | 已知行为，文档说明；不做黑名单 |
| 时区：`edition_date` 用 UTC 日切，用户在中国看到"今天"可能少几条 | 前端用 `published_at` 显示本地时间；`edition_date` 只用于分组。**这个偏差是设计选择，写进测试注释** |
| 周报素材超上下文 | `weekly-max-items` 截断 + 两轮分段（可配） |
| 空候选池（所有源都失败） | 不调 AI、不写行、返回统计；页面显示当天无内容而不是报错 |
