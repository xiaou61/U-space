---
artifact: proposal
work_id: WORK-014
work: 源码解读专题与交互式代码走读
status: draft
workflow: full
depends_on: [requirements]
related_to: [WORK-009, WORK-010, WORK-011]
created: 2026-10-17
updated: 2026-10-17
---

# 提案 · 源码解读专题与交互式代码走读

## 摘要

把它做成**知识库里的一个专题类型 + 两种可交互的正文块**：`code-tour`（逐步代码走读）与 `quiz`（即时反馈小测）。代码走读的数据、解释与出处都写在条目正文里，渲染复用 `@paideia/ui` 已有的 markdown 管线与 `CustomRenderer` 扩展点；AI 联动只加"预填一句话"的桥，因为助手请求**已经携带当前条目完整正文**。

不新增后端模块、不加表、不加端点。真正常新增的只有：两个渲染块、Shiki 语法高亮、助手预填桥、首页入口与一篇样板内容。

## 推荐方案

### 机制

1. **内容落位**：`源码解读` 作为知识库顶级分类；一篇 = 一个机制。正文仍是 markdown。
2. **走读块**：正文里写 ```` ```code-tour ````，内容是 JSON（repo / commit / license / file / steps）。`Markdown` 通过 `plugins.renderers` 把它交给 `CodeTour` 组件：
   - 左侧/上方是代码，使用 `CodeBlock` 的 `startLine` 显示真实行号；
   - `focus` 行加高亮背景；
   - 右侧/下方是当前步骤标题、解释、上一步/下一步、步骤点；
   - 组件内部只维护 `activeStep` 一个状态，切步骤不重新解析正文。
3. **出处**：块底部自动生成 `repo@commit · path · Lx-Ly · license`，并给出 GitHub permalink。permalink 由 `repo`/`commit`/`file`/行范围拼出；`repo`、`commit`、`file` 用白名单正则校验，避免拼出非 GitHub 链接。
4. **测题块**：`quiz` JSON 渲染成题干 + 选项 + 解析；点选即判对错。状态只在组件内，刷新重置。
5. **高亮**：在 `@paideia/ui` 的 `Markdown` 里接 `@streamdown/code` 的 `code` 插件；语言白名单，语法包按需加载。
6. **AI**：`CodeTour` 的按钮调用 `onAskAssistant(prompt)`；`MarkdownBody` 把页面注入的回调透传下去；`KnowledgeEntryPage` 从 `useAssistantContext()` 取 `ask()`。`ask()` 只做"打开面板 + 填输入框"，不发送。管理端预览不传回调，按钮不显示。
7. **入口**：首页 `QUICK_LINKS` 增加一项；知识库分类页自然出现该分类。

### 为什么它最满足需求

- **互动**来自走读推进与即时测题，不依赖新的运行时。
- **可视化**来自"代码 + 行高亮 + 步骤 + 架构图（图片）"，且不需要重构编辑器。
- **AI 联动**零后端成本：整篇正文已在系统提示词里，预填一句话就能问"第 3 步为什么"。
- **可维护**：正文与代码片段同一份来源，预览与线上同一条渲染管线；没有第二套内容系统。

## 范围

**v1 包含**

- `源码解读` 分类与一篇样板条目；
- `code-tour` 块：解析、校验、渲染、键盘/窄屏、降级；
- `quiz` 块：即时判对错与解析；
- Shiki 语法高亮 + 语言白名单 + 懒加载；
- 三个 AI 预填入口（每步「问 AI」、篇末「让 AI 考考我」「换个讲法」）；
- 首页入口；
- 单元/组件/e2e 测试与作者指南。

**v1 不包含**

- Mermaid（v1.5，先量体积）；
- 自动索引/自动生成；
- 代码执行、逐行状态机；
- 阅读进度、答题记录、收藏、评论；
- 任何后端/数据库改动。

## 备选方案

### A. 独立"源码解读"模块（专用表 + 专用编辑器）

**最强理由**：内容模型更干净，可结构化查询、可做进度与统计，将来接自动生成也顺。
**为什么不选**：它会复制知识库已有的分类、条目、正文、图片、目录、关系、自评与 AI 检索；v1 的收益（两种块）远小于这套重复。等真的需要"按步骤统计正确率"时再谈建表，届时也可以只加一张答题记录表，而不是重做内容系统。

### B. 纯 markdown（代码块 + 图片 + 文字）

**最强理由**：今天就能发，零代码。
**为什么不选**：没有逐步推进、没有即时反馈，"互动"这条不成立；代码块的解释只能靠长段落，读者容易迷路。

### C. 自动索引任意 repo（DeepWiki 式）

**最强理由**：内容成本接近零，覆盖面最大。
**为什么不选**：生成内容仍需人工核行号与结论；幻觉与许可证风险都落在我们身上；运行时抓取还引入 rate limit 与外部依赖。用户已明确"都是我们内置的"。

### D. 引入一个现成的 tour/教程框架

**最强理由**：少写解析与视图。
**为什么不选**：CodeTour 是 VS Code 扩展，依赖编辑器 API；教程框架（如 Sandpack/Scrollytelling）解决的是"可运行代码"或"滚动叙事"，与我们的"行级走读 + 条目正文"不匹配，还会多一套依赖与样式。

## 仓库影响

| 位置 | 影响 |
| --- | --- |
| `frontend/packages/ui/src/markdown/renderers/code-tour.tsx`（新增） | 解析、校验、视图、出处页脚 |
| `frontend/packages/ui/src/markdown/renderers/quiz.tsx`（新增） | 测题视图 |
| `frontend/packages/ui/src/markdown/markdown.tsx` | 接 `@streamdown/code`；文档模式合并平台渲染器 |
| `frontend/packages/ui/package.json` | 新增 `@streamdown/code`（钉版本） |
| `frontend/packages/ui/src/styles/globals.css` | `@source` 指向 `@streamdown/code/dist`（漏了会静默缺样式） |
| `frontend/packages/knowledge/src/markdown-body.tsx` | 透传 `onAskAssistant`；启用文档渲染器 |
| `frontend/packages/assistant/src/context.tsx` | 增加 `ask(prompt)` 与 `pendingPrompt` |
| `frontend/packages/assistant/src/assistant-shell.tsx` | 收到 `pendingPrompt` 时打开面板并切到对话页 |
| `frontend/packages/assistant/src/conversation-panel.tsx` | 消费 `pendingPrompt`，填入输入框并清空 |
| `frontend/apps/app/src/pages/KnowledgeEntryPage.tsx` | 注入 `ask` |
| `frontend/apps/app/src/pages/HomePage.tsx` | `QUICK_LINKS` 增加「源码解读」 |
| `frontend/apps/admin/src/pages/KnowledgeEntriesPage.tsx` | 不改代码；预览经 `MarkdownField` 自动获得新块 |
| 测试 | `ui` 解析/视图单测、`knowledge` 透传单测、`assistant` 预填单测、学习者端与管理端 e2e |

## 交付拆分

| 切片 | 内容 | 可独立验证的结果 |
| --- | --- | --- |
| 1 | 走读块解析 + 视图 + 单测 | 给定 JSON，渲染出正确步骤与行号；坏数据降级 |
| 2 | 接入 `Markdown`/`MarkdownBody` + 预览一致 | 学习者页与管理端预览显示同一结构 |
| 3 | Shiki + 白名单 + 懒加载 | 白名单语言着色；主包体积不变 |
| 4 | 测题块 + 单测 | 点选即时判对错 |
| 5 | AI 预填桥 | 点「问 AI」打开面板并填好提示，未发送 |
| 6 | 首页入口 + 样板条目 + e2e | 从首页可走完一篇；无 GitHub 请求 |

## 风险与缓解措施

| 风险 | 缓解 |
| --- | --- |
| **许可证** | 只做 MIT/Apache-2.0/BSD 选题；页脚强制显示许可证；GPL/AGPL 只链接不内嵌；保留下架路径 |
| **行号漂移** | 所有片段锚定固定 `commit`，不跟 `main`；permalink 指向该 commit |
| **体积** | Shiki 懒加载并限制语言；Mermaid 暂不引入；构建后核对主包体积 |
| **解析/渲染安全** | 代码按文本渲染；`explain` 用同一套净化 markdown 且不递归启用自定义块；外链白名单 |
| **坏内容炸页** | 每个块自带 try/catch 与错误边界；schema 上限；降级为普通代码块 |
| **内容成本** | 先做一篇样板并把作者指南写清；AI 只起草，行号与结论人工核 |
| **JSON 难手写** | 提供可复制的模板与校验错误信息；实施时再评估是否需要离线小工具，不在 v1 做编辑器 |

## 验收映射

| 需求 | 验收 |
| --- | --- |
| REQ-001 | AC-001、AC-008、AC-012 |
| REQ-002 | AC-001、AC-010 |
| REQ-003 | AC-003 |
| REQ-004 | AC-002 |
| REQ-005 | AC-004 |
| REQ-006 | AC-005 |
| REQ-007 | AC-006、AC-007 |
| REQ-008 | AC-008 |
| REQ-009 | AC-009 |
| REQ-010 | AC-010 |
| REQ-011 | AC-011 |
| REQ-012 | AC-013 |
| REQ-013 | AC-012 |

## 待决定事项

1. Mermaid 放 v1 还是 v1.5（建议 v1.5，先量体积）。
2. 第一篇样板选 React 还是 Spring Boot（不影响实现）。
3. 小测是否每篇必做（建议是）。
