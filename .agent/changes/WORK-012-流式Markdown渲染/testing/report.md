---
artifact: report
work_id: WORK-012
work: 流式 Markdown 渲染（终止·增量·降级）
status: passed
evidence: required
verified_commit: 3793a31
verified_at: 2026-10-16
---

# 验收报告 · 流式 Markdown 渲染

## 结论

**通过。** 助手回答现在按 markdown 渲染，且渲染器只剩一套——它从 `@paideia/knowledge` 挪进 `@paideia/ui`，知识库条目、管理端预览、助手回答共用；`react-markdown` / `remark-gfm` 两个依赖随之删除。

比"渲染出来了"更值得记的是**本项把"百分百"变成了可红的断言**：语料是真实上游的增量序列（离线固化进仓库），11 条语料 + 30 条恶劣输入**逐字符**回放，任何前缀的"已完成块"都等于最终渲染，**0 例不一致**；增量式的重渲染次数另有确定性计数（合成 20 段时每次新增最多 2 次），并与"每次从零挂载"的对照组拉开数量级。

顺带**修掉两处静默缺样式**——都是这次接线带进来、且控制台不会有任何提示的：`@source` 指到了不存在的目录（整行形同虚设，而且"看起来生效"，因为我自己的注释里恰好写了那个类名），以及 `streamdown` 在用、我们没定义的 `--color-sidebar` 令牌。两处都补了会失败的检查。

体积不是"变大了"：**主 chunk 1203 kB → 697 kB**（比接线前的 836 kB 还小 139 kB，因为顺手去掉了 react-markdown），渲染器成 505 kB 的按需 chunk。

## 如实记下的三处偏差

1. **AC-006「流式过程中表格/代码块不闪」没有 e2e 覆盖。** 既有学习者端 e2e **故意不调用真实上游**（它验的是"积分不足时不许打到上游""不会偷偷发起生成"），所以没有"真的在流式"的场景可断言。本项补了三条**真浏览器**断言，但打的是**定稿后的**正文：结构（真 `<table>`/真代码块）、计算样式（代码块底色非透明且不同于页面底色、行号伪元素有内容）、深浅两套主题。**"过程中不闪"这一条只有单测口径**（逐前缀等价 + `disabled` 的复制/下载），**登记为未覆盖**，不当作已验。
2. **两处按场景不同的策略是故意的**，不是实现分叉：外链在流式回答里走"确认后打开"（模型生成的链接先问一次）、在长文里直接给 `<a>`（管理员写的）；表格的复制/下载下拉只给聊天场景。已在测试里各自钉住。
3. **上游一处小瑕疵**：补全出未完成的图片时，它把占位块渲染成 `<div>` 而位置在 `<p>` 里，React 会在开发模式报 `In HTML, <div> cannot be a descendant of <p>`。触发面是"模型正好吐到 `![](` 的那一帧"，用户看不到坏掉的东西，**不修**，已记进 `notes/dev-workflow.md`。

另外：**深浅两套主题的观感**我只做到"计算样式确实换了一套取值、且都非透明"，**"好不好看"要用户真人看**——那是他的判据。

## 验收矩阵

**证据口径**：`退出码` 列写数字的是本次**实际跑过**的命令；写 **人工** 的是需要真人（视觉）或需要真机上游才能做的部分。

| 检查项 | 命令 | 退出码 | 结果 | 证据 |
| --- | --- | --- | --- | --- |
| AC-001 渲染生效 | `pnpm --filter @paideia/app test:e2e`（`markdown-render.spec.ts`） | 0 | 通过 | 33 passed / 0 failed（2.1 分钟）；新 3 条断言条目正文里有真 `<table thead th>`×2、`tbody tr`×2 与 `[data-streamdown="code-block"]` |
| AC-002 未闭合不破形 | `pnpm --filter @paideia/ui test`（`markdown.test.tsx`） | 0 | 通过 | 3 条：未闭合围栏 → **1 个**代码块；`正文\n\n```java\nclass A {` → 2 块且第一块仍是 `<p>正文。</p>`；未闭合强调不吞后续正文 |
| AC-003 逐前缀等价 **0 例不一致** | 同上 | 0 | 通过 | 11 条真实语料（25–1658 个增量）全部 0 不一致；每条都断言 `checked > 0`（不允许"没有可比较的块"混过去）；另有反例自检：改一个块必须被判为不一致 |
| AC-004 增量真的省 | 同上 | 0 | 通过 | 合成 20 段：`max(每次新增的重渲染数) ≤ 2`、总量 ≤ 2×(段数−1)；对照组"每次从零挂载" > 增量式×2；11 条语料：增量式 ≤ 重装甲，且多块语料**严格更便宜** |
| AC-005 降级可见且不跳 | 同上 | 0 | **通过（口径与工件原话不同，见下）** | 用"搜索"代替"写一个降级分支"：30 条恶劣输入**逐字符**回放，**0 例不一致**。另有两条：`streamdown:incomplete-link` 进不了 `href` 且点它不调 `window.open`；`streamdown:incomplete-image` 渲染成占位块而非真 `<img>` |
| AC-006 表格/代码块不闪（e2e） | — | — | **未覆盖** | 见上「偏差 1」。既有 e2e 不打上游，本机没有"真流式"可断言 |
| AC-007 安全回归 | `pnpm --filter @paideia/ui test` | 0 | 通过 | 6 条，且**两种模式都查**：`<script>` 不进 DOM 也不执行、`javascript:` 不进 `<a href>` 也不进 `<img src>`、代码块里的 HTML 保持字面量 |
| AC-008 单一实现 | 同上 + 构建产物 | 0 | 通过 | 两种模式渲染同一 markdown 得到**同一副元素骨架**；表格与代码块**连属性一致**（唯一差别是流式下复制/下载 `disabled`，已显式断言）；`@paideia/knowledge` 已无 `react-markdown`/`remark-gfm` 依赖 |
| 体积：主包不因它变大 | `pnpm --filter @paideia/app build` | 0 | 通过 | 主 `index` **697.45 kB**（gzip 219 kB）+ 按需 `dist` **504.93 kB**（gzip 154 kB）；接线前主包 836 kB、接线未懒加载时 1203 kB |
| 样式真的生效（真浏览器） | `pnpm --filter @paideia/app test:e2e` | 0 | 通过 | 行号伪元素 `content` 不是 `none`（该 class 只在它 dist 里）；代码块底色非透明且 ≠ `body` 底色；切深色后两者取值都变 |
| 令牌与扫描的守卫 | `pnpm --filter @paideia/ui test` | 0 | 通过 | `streamdown-tokens.test.ts`（2 条，逐个断言它用到的颜色令牌可解析）+ `source-coverage.test.ts`（新增 1 条：每个 `@source` 指向的目录必须真实存在） |
| 全仓类型与单测不回归 | `pnpm typecheck` / `pnpm test`（frontend 根） | 0 / 0 | 通过 | typecheck 全绿；单测：ui **93**（markdown 73 / tokens 1 / contrast 10 / theme-context 5 / streamdown-tokens 2 / source-coverage 2）、knowledge 20、assistant 16、auth 13 |
| 管理端草稿预览不回归 | `pnpm --filter @paideia/admin test:e2e` | 0 | 通过 | 8 passed / 0 failed（47.8 秒），含"录入到发布的链路" |
| 深浅两套主题观感 | 人工 | 人工 | **待用户** | 见上（计算样式已验，"好不好看"由用户判） |

## 反向验证（"这条要是坏了，哪个断言会红"）

| 被验行为 | 反着改一处 | 结果 |
| --- | --- | --- |
| `@source` 真的扫到了 streamdown 的类 | 把它指回仓库根的 `node_modules/streamdown/dist/*.js`（**接线时的真实写法，那里什么都没有**） | 构建产物里 `--sdm-c`、`counter(line)`、`contain-intrinsic-size` **全部 0 命中**，CSS 88.49 → **78.79 kB**；而 `bg-sidebar` 仍在（来自我自己的注释）——**这就是当时那次假绿**。修好后四者都出现 |
| `--color-sidebar` 令牌是必要的 | 删掉 `--color-sidebar` / `--color-sidebar-foreground` 两行 | 构建产物里 `bg-sidebar` 与 `border-sidebar` 规则数**都是 0**（Tailwind 只为已定义的主题值生成类，且不报错）；补回后 `.bg-sidebar{background-color:var(--muted)}` |
| AC-003 的比较不是恒真 | 把第二个块的文字改一个字 | 该用例**红**（`'<p>乙段</p>'` ≠ `'<p>乙段改了</p>'`），且这一条已固化成常驻用例 |
| AC-004 的计数确实在动 | 看对照组的数 | 从零挂载的次数随段数平方级增长，必须 > 增量式×2；两条都断言 `> 0`，计数器坏掉时会先红 |
| 行号的类真的来自 streamdown | 建一份不带 `@source` 那一行的产物 | 真浏览器里 `getComputedStyle(span, '::before').content` 会读到 `none`——jsdom 验不了这条，所以它是 e2e、不是单测 |

## 本次一并发现、但**不属于本项**的两件事

1. **学习者端 e2e 的会话只用 `storageState` 会从第二条用例起掉到登录页**：本目录的既有约定是**每条用例先 `signIn(page)`**（有会话就复用、没有就登录）。我最初只带 `storageState` 深链，第 1 条过、第 2、3 条落登录页；按约定补 `signIn` 后三条全过。既有 spec 都遵守这条，所以**不是缺陷**，记录以免下次重踩。
2. **`backend/config/.playwright-cli/page-*.yml` 四个文件未被 `.gitignore` 忽略**（playwright-cli 的页面快照，不是本项目产物；同目录下的 `application-local.yml` 是正确忽略的）。任何人 `git add -A` 都会把它们带进公开仓库。**建议加一条忽略规则**，本项未擅自改 `.gitignore`。
