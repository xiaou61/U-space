---
artifact: research
work_id: WORK-027
work: 每日技术速递
status: active
created: 2026-10-20
updated: 2026-10-20
---

# 每日技术速递 · 形态参考与数据源实测

`requirements.md` 只保留结论，过程与数字在这里。

## 一、AIHOT 是什么（联网核对）

来源：GitHub `KKKKhazix/AIHOT` README、`khazix-skills/aihot` SKILL.md、腾讯云社区《从零搭一个会自己判稿的行业热点站：AIHOT 实战》。

### 产品形态

一个「行业热点站」引擎。作者原话：「一个自己找热点、自己写日报的网站框架。把信源换成你的，把精选标准换成你的 KnowHow，它就是你的行业热点站。」

### 六步流水线

| 步 | 做什么 | 对我们的可借鉴点 |
| --- | --- | --- |
| 1 采集 | 六种信源：RSS、网页列表、JSON 接口、X 账号、微信公众号、自推 | 我们只做 RSS + 官方 API 两种，够了 |
| 2 预筛 | 先用便宜规则去掉明显不行的 | 我们用「源自身信号」（HN 点数、star 增量）代替 |
| 3 评分 | **同一份标准独立打两次分**，按信源分级定门槛 | **不学**：一天一次调用是硬约束，打分两次 = 两倍成本 |
| 4 写作 | 中文标题、答案先行的摘要、推荐理由、外文全文翻译 | **学摘要与中文标题**；不做全文翻译 |
| 5 聚簇 | 同一件事聚成一个事件，模型判断「同一件事 / 后续进展 / 两件事」 | **不学**：这是它真正的护城河，也是它最贵的一步（要向量或文字重合 + 模型判关系 + 写入前再复核） |
| 6 成刊 | 日报按规则编，周报月报从日报汇编 | **学日报**；周报放二期（X-05） |

### 热度算法（值得抄的一条）

> 48 小时内，每个独立来源只算一次，24 小时减半。重复抓取不会多算，一家媒体发十篇也只算一次。

这条纪律解决了「同一来源刷屏」问题。我们一期没有多源聚簇，但**同一个源内按 URL 去重**是它的简化版。

### 它的对外接口（我们明确不套壳）

`aihot.news/api/v1/agent/latest|search|hot|daily`，匿名 GET，返回整理好的中文 Markdown。

2026-10-20 实测 `GET https://aihot.news/api/v1/agent/latest`：**200 / 8357 字节**，返回形如：

```
# AIHOT 简报 · 过去 24 小时
10 条，从新到旧，时间为北京时间。后面还有，调大 limit（最大 30）可以多看。
...
1. [Redact 调查：WhatsApp 帮助页面隐藏 LLM 指令…](https://aihot.news/items/...)
   Hacker News：AI 热榜 · 发布于 10-20 00:00 · 行业
   摘要：…
   推荐理由：…
   原文：https://redact.dev/blog/...
```

它的 `terms` 写明：个人非商业、公益非商业、组织内部免费；**对外商业用途须书面授权**。所以把它的接口当数据源既不是"自己做"，也带着授权问题。**只作为产品形态参考。**

### 技术栈对比

AIHOT 是 TypeScript 单仓三进程（web 3000 / api 3001 / worker）+ PostgreSQL。我们是 Spring Boot 模块化单体 + MySQL。**形态可借鉴，实现完全不通用**——本项不复用它的任何代码。

## 二、数据源可达性实测

### 测试条件

- 本机：Windows / Git Bash，`curl.exe`
- 生产：`codenest-online`（36.212.5.130），SSH 内执行 `curl`
- 全部匿名，不带 key、不带 Cookie
- 时间：2026-10-20 下午

### 逐源结果

| 源 | URL | 本机 | 生产机 | 备注 |
| --- | --- | --- | --- | --- |
| Hacker News | `https://hn.algolia.com/api/v1/search?tags=front_page&hitsPerPage=N` | **200** | **200 / 3607B** | 字段完整：`title`/`url`/`points`/`num_comments`/`created_at_i`/`story_id`。实测样本 `points=551, num_comments=488` |
| GitHub 热榜页 | `https://github.com/trending?since=daily` | **时通时断**：一次 200/155KB，之后连续三次 `Failed to connect` | **200 / 593783B**，12 行可解析 | 页面结构：`<article class="Box-row">`，内含 `<h2 class="h3 lh-condensed">` 与 repo 链接、`<p class="col-9...">` 描述、`N stars today`、`itemprop="programmingLanguage"` |
| GitHub Search | `https://api.github.com/search/repositories?q=created:>2026-10-13 stars:>100&sort=stars` | **200**，`total_count=207` | 未测 | 匿名限额实测：**search 10/分、core 60/时**（`rate_limit` 返回） |
| GitHub API 通用 | `https://api.github.com/rate_limit`、`/advisories` | 200 | 200 | 可作为 Releases 追踪通路 |
| arXiv API | `https://export.arxiv.org/api/query?search_query=cat:cs.AI&sortBy=submittedDate&max_results=3` | **200 / 9476B** | **200 / 9476B** | Atom feed，含标题/作者/摘要/提交时间 |
| arXiv RSS | `https://export.arxiv.org/rss/cs.AI` | 200 但**仅 892B** | 200 / 892B | 是合法 RSS 但内容基本为空壳，**不用** |
| Hugging Face 日榜 | `https://huggingface.co/api/daily_papers` | **超时** | **超时** | 不可用 |
| 量子位 | `https://www.qbitai.com/feed` | 200 / 6345B，**10 条** | 200 | **条目无正文**（`content:encoded` 不存在），只有标题/链接/`pubDate`。标题示例「Claude新模型曝光！能跳出思维定势，修Bug治标又治本」 |
| IT 之家 | `https://www.ithome.com/rss/` | 200 / 281058B，**60 条** | 200 | 量大，需要按时间窗裁剪 |
| InfoQ 中文 | `https://www.infoq.cn/feed` | 200 / 161438B，**20 条** | 未测 | 带 HTML 正文（`&lt;blockquote&gt;`…） |
| 掘金 | `https://juejin.cn/rss` | 200 / 15763B | 未测 | — |
| 开源中国 | `https://www.oschina.net/news/rss` | 200 / 42981B | 未测 | — |
| OpenRouter 模型库 | `https://openrouter.ai/api/v1/models` | **200 / 788314B**，**470 个模型** | **200** | 字段含 `id`/`name`/`created`/`pricing{prompt,completion}`/`context_length`。实测首条 `minimax/minimax-m3.1-flash:free`，pricing 全 0 |
| 机器之心 | `https://www.jiqizhixin.com/rss` | 302 → `/data-service`（返回 HTML） | 返回 HTML 不是 RSS | 不可用 |
| Solidot | `https://www.solidot.org/index.rss` | **403** | 未测 | 不可用 |
| LMArena | `https://lmarena.ai/api/leaderboard` | 超时 | 未测 | 不可用 |
| Product Hunt | `https://www.producthunt.com/feed` | TLS 错误 `CRYPT_E_REVOCATION_OFFLINE` | 未测 | 不可用（本机 schannel 吊销检查问题，不一定是站点问题，但不值得为它折腾） |

### GitHub 热榜解析可行性

生产机上对 `/tmp/t.html` 用正则抽取，第一行结果：

- `rows 12`（当日 12 个仓库）
- 描述：`The job search that runs on your machine. AI job application…`
- 当日新增 star：`3,856`
- 语言：`Python`
- repo 路径：正则 `href="/([^"]+)"` 会先撞到赞助商链接，**必须先定位 `<h2>` 块再取 href**（实测第一次抽取返回空，第二次在 `<h2>` 内取才拿到 `anomalyco/opencode`）

**这条细节直接决定了实现难度**：解析器不是「一行正则」，而是一段有状态的小解析。而 `github.com/trending` 本机三次连不上、生产 200——**同一份代码在不同网络环境下结果不同**，所以必须保留 Search API 兜底（I-05）。

### robots 与合规

- `github.com/robots.txt` 对通用 UA 没有整体 Disallow，但**明确点名了 GPTBot / OAI-SearchBot / ClaudeBot / anthropic-ai / PerplexityBot** 并给了 `Crawl-delay: 1`。我们的 User-Agent 是自建标识（`PaideiaBot`），不在名单里；即便如此，**每天一次、只取一页**，实际压力可忽略。
- 各 RSS 源是**主动提供**的订阅接口，抓取它们是它们的用途本身。
- arXiv 明确提供 API 并鼓励程序化访问。

## 三、可复用的既有实现（避免重写）

| 需要的东西 | 现成的 | 位置 |
| --- | --- | --- |
| 定时任务形态 | `@Scheduled(fixedDelay=…, initialDelay=…)` + 自己接住异常 | `TelemetryRetentionPurger`、`ExpiredStatePurger`、`EssayCollectionJob` |
| 条件装配 | `@Configuration` + `@ConditionalOnProperty` 内嵌静态类 | `EssayConfiguration` |
| 配置类 | `@ConfigurationProperties` + 默认关闭 | `EssayProperties` |
| 抓取端口 | 接口 + 单实现 + 上游变更只换一个类 | `EssaySource` / `NowcoderEssaySource` |
| AI 调用 | AgentScope OpenAI 兼容 + `DeepSeekFormatter` + `blockLast()` | `EssayExtractor` |
| AI 用量分类 | 加一行常量，统计页自动多一个筛选值 | `ai/RunKind.java` |
| 指纹去重 | 规范化 + SHA-256 + 先查后插（可空列上不建唯一约束） | `EssayCollectionService.fingerprint` |
| 写库去重语义 | `CreateOutcome(entryId, created)` | `KnowledgeApi.createCollected` |

**结论：本项没有一项技术是新的**——每一样在 WORK-025 里都跑过了。这是它风险可控的主要原因。

## 四、成本估算

- 每日候选池：三组源合计约 40–60 条（新闻 30 + 热榜 12 + HN 10）。
- 输入 token：每条按标题 + 短描述 100 token 估，约 5000 token；加提示词约 7000。
- 输出 token：每条一句中文摘要约 60 token，60 条约 3600；加挑选理由约 4500。
- **单日约 1.2 万 token，一次调用**。按 DeepSeek 价目表属可忽略量级。
- 对照：若照面经形态「每条一次调用」，60 条 = 60 次调用，且每次都要带提示词，**成本约为本方案的 30 倍**。

## 五、风险清单

| 风险 | 影响 | 缓解 |
| --- | --- | --- |
| `github.com/trending` 页面改版 | 热榜整组为空 | Search API 兜底（I-05）；兜底也坏时该组为空但页面正常 |
| RSS 源停更 / 改地址 | 少一组新闻 | 端口化，一源一实现，单源失败不影响其它 |
| 量子位等源无正文 | 摘要质量取决于标题 | 已知降级；提示词要求「信息不足就说标题说了什么」，不编造 |
| AI 摘要失败 | 只有原标题 + 链接 | 降级路径（AC-005） |
| 抓取被风控 | 该源失败 | 每天一次、可识别 UA、优先官方 API |
| 平台 AI 凭据未配 | 无摘要 | 同上，降级 |

## 六、扩展点候选（Agent 提，用户按需裁）

| 编号 | 扩展点 | 成本 | 价值 | 备注 |
| --- | --- | --- | --- | --- |
| X-01 | **模型上新与降价**（OpenRouter `models` 每日 diff） | 极低：一次 GET，零 AI | 高：学习者最关心「哪个模型便宜了/新出了什么」 | 已实测 200 / 470 个模型含 pricing |
| X-02 | **arXiv 每日论文**（cs.AI / cs.LG / cs.CL，按提交时间） | 低：官方 API，一次请求 | 中高：前沿感最强，但英文标题需要翻译（并入一次调用） | 已实测 200 |
| X-03 | **关键仓库版本更新**（GitHub Releases：vllm / langchain / spring-boot …） | 低：官方 API | 中：对「跟着版本学」有用 | 需维护一份仓库清单 |
| X-04 | **中文社区热帖**（掘金 / 开源中国 / InfoQ） | 低：RSS | 中：中文读者门槛低 | 已实测均 200 |
| X-05 | **周报**：从日报聚合，AI 写一段「本周技术主线」 | 中：多一次每周调用 | 中高：比日报更有「回顾」价值 | 依赖日报先跑稳 |
| X-06 | **按身份筛源**（求职者多看招聘/面经，学生多看论文） | 中：要改查询与展示 | 中：与 WORK-010 的自然延伸 | 二期 |
| X-07 | **与学习线联动**（「这条新闻涉及 RAG，你的线第 7 关讲过」） | 高：需要内容与条目的匹配 | 高：这是「不是又一个 RSS 阅读器」的关键 | 二期；关键词匹配可先做，向量检索项目级不做 |
| X-08 | **速递 → 笔记/知识库**（一条速递一键存成笔记草稿或知识条目） | 中：复用笔记模块 | 中：把「读」变成「留」 | 二期 |

**X-01 的理由**：它是唯一一条「零 AI、零抓取风险、每天一次 GET」的扩展点，而产出的是最硬的信息（谁降价了、谁新发了模型）。与一期一起做的增量成本几乎为零。
