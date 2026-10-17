# 源码解读：作者指南

这篇是给**写内容的人**看的。它讲清五件事：一篇合格的标准、五种块的写法、字段上限、许可证口径、以及怎么用 AI 起草而不被它骗。

渲染与校验的实现在 `frontend/packages/ui/src/markdown/renderers/schema.ts`，报错文案与这里的上限是同一份。

## 一篇合格的标准

- **一篇只讲一个机制**。不是"React 概览"，而是"一次并发渲染是怎么开始的"。
- 结构固定：**架构地图 → 走读（5–12 步）→ 至少 1 个预测、至少 1 个状态快照 → 练习 → 小测 → 三个常见误解 → 面试追问**。
- 每段代码**锚定固定 commit**，页脚自动带 `repo@commit`、行范围与许可证。
- 3 道小测是建议下限；练习至少一道。
- 20–40 分钟能读完。写不完就是该拆成两篇。

## 五种块

围栏语言就是块类型。**JSON 必须是合法 JSON**（不能写注释、不能有尾逗号）；任何一块出错时只会降级那一块，并显示"第一个不满足的字段"。

### 1. 学习路径 `learning-path`

专题总览用。`entryId` 省略时该站不可点击（适合先占位的预告）。

```json
{
  "title": "读懂 React 的更新流程",
  "steps": [
    { "entryId": 12, "title": "从一个 setState 说起", "summary": "先建立全局图景", "minutes": 12 },
    { "title": "调度：Lane 与优先级", "minutes": 20 }
  ]
}
```

上限：1–20 站；`minutes` 1–600。

### 2. 架构地图 `code-map`

节点按 `layer`（从左到右）与 `order`（同列自上而下）摆放。`tour` + `step` 指向同页走读的某一步，点它就会跳过去；走读切步时，对应节点会高亮。

```json
{
  "title": "从调度到渲染",
  "nodes": [
    { "id": "schedule", "label": "scheduleUpdateOnFiber", "layer": 0, "order": 0 },
    { "id": "render", "label": "renderRootConcurrent", "layer": 1, "order": 0,
      "file": "packages/react-reconciler/src/ReactFiberWorkLoop.js", "line": 2794,
      "tour": "react-render", "step": 0 }
  ],
  "edges": [{ "from": "schedule", "to": "render", "label": "调度" }]
}
```

上限：节点 1–24、边 ≤ 48、`layer/order` 0–12；边的两端必须存在。**节点超过 24 个就拆成两张图**，不做自动布局。

### 3. 代码走读 `code-tour`

```json
{
  "id": "react-render",
  "title": "渲染是怎么开始的",
  "repo": "facebook/react",
  "commit": "d75b0697",
  "license": "MIT",
  "file": "packages/react-reconciler/src/ReactFiberWorkLoop.js",
  "steps": [
    {
      "title": "要不要从头开始",
      "line": 2800,
      "code": "if (workInProgressRoot !== root || ...) {\n  prepareFreshStack(root, lanes);\n}",
      "language": "javascript",
      "focus": [1, 2],
      "explain": "条件只有两个：root 变了，或本轮的 lanes 变了。",
      "predict": {
        "question": "为什么换了 root 就必须丢掉旧栈？",
        "options": ["旧栈属于另一次渲染", "为了释放内存", "为了触发 GC"],
        "answer": 0,
        "reveal": "workInProgress 指向的是某一轮渲染的半成品树。"
      },
      "state": {
        "vars": [{ "name": "lanes", "value": "SyncLane" }],
        "stack": ["prepareFreshStack", "renderRootConcurrent"],
        "heap": [{ "id": "root", "label": "FiberRoot", "fields": { "tag": "3" } }],
        "output": []
      }
    }
  ]
}
```

规则与上限：

| 字段 | 规则 |
| --- | --- |
| `id` | 必填、≤ 64；地图靠它联动 |
| `repo` | `owner/name` |
| `commit` | **7–40 位小写十六进制**。写 `main` 会被拒绝——片段必须钉死 |
| `file` | 仓库内相对路径，不能以 `/` 开头、不能含 `..` |
| `license` | ≤ 40 字符，原样展示 |
| `steps` | 1–20 |
| `line` | 该段代码在原文件里的起始行 |
| `code` | ≤ 200 行、≤ 20000 字符 |
| `focus` | 可选，1-based **块内行号**，必须落在代码行数内，≤ 20 个 |
| `explain` | ≤ 2000 字符，支持 markdown |
| `predict` | 可选；2–4 个选项，`answer` 从 0 起 |
| `state` | 可选；`vars` ≤ 30、`stack` ≤ 30、`heap` ≤ 20、`output` ≤ 30 行 |

**预测的语义**：带 `predict` 的步骤在作答前**不显示解释**，答完才揭晓；「直接看解释」是逃生口。所以 `explain` 要写成"揭晓后仍然成立"的版本，别写成"你答对了吗"。

**状态快照**是给读者看的，不是程序算出来的：`stack` 第一个元素当栈顶显示，`heap` 每项写 `id` + `label` + 可选字段。

### 4. 动手练习 `code-challenge`

三种题型，全部客户端判定，**不执行代码**（真正的运行在 WORK-015）。

填空：模板里的 `{{0}}`、`{{1}}` 是空位，每空一组选项。

```json
{
  "id": "fill-1", "kind": "fill", "title": "补上重置逻辑",
  "template": "function prepare(root) {\n  {{0}}(root);\n}",
  "options": [["prepareFreshStack", "createRoot"]],
  "answers": [0],
  "explain": "先重置工作栈，再进入循环。"
}
```

排序：`lines` 是展示顺序（**故意打乱**），`answer` 是正确顺序的索引排列（必须是 0…n-1 的一个排列）。

```json
{
  "id": "order-1", "kind": "order", "title": "把顺序排对",
  "lines": ["判断是否要重置", "标上下文", "prepareFreshStack", "workLoopConcurrent"],
  "answer": [1, 0, 2, 3],
  "explain": "先标上下文，再判断要不要重置。"
}
```

找 bug：`bugLine` 是 1-based 行号。

```json
{
  "id": "bug-1", "kind": "bug", "title": "这段错在哪一行",
  "code": "if (...) {\n  resetRenderTimer();\n}\nprepareFreshStack(root, lanes);",
  "bugLine": 4,
  "explain": "prepareFreshStack 应该在条件里面。"
}
```

上限：填空的模板空格数必须等于选项组数；排序 2–60 行；找 bug 的代码 ≤ 60 行。

### 5. 小测 `quiz`

```json
{
  "id": "q1", "title": "三个问题",
  "questions": [
    { "question": "什么情况下会接着上次的进度？", "options": ["root 与 lanes 都没变", "只有 root 没变"], "answer": 0, "explain": "两个条件任一变化都会重置。" }
  ]
}
```

上限：1–10 题、2–6 个选项；点选即判、**不能改答案**（避免改到对为止）；块末显示得分。

## 许可证口径

- **优先 MIT / Apache-2.0 / BSD**。这三类只要求保留版权声明与许可声明，教学引用并注明出处即可。
- **GPL / AGPL**（Linux、Redis 等）：不内嵌代码，只链接受害最小；确实要引就只引极短片段并保留完整版权头。
- 每个走读块页脚自动显示 `license`，**正文末尾再写一句出处声明**（样板见种子条目）。`license` 字段只用于展示，不参与任何法律判断。
- 不确定时先不写这篇——**选题换掉比事后下架便宜**。

## 行号怎么核

1. 先在固定 commit 上打开文件（`https://github.com/<repo>/blob/<commit>/<path>`），确认版本。
2. 用编辑器的行号，或 `grep -n` 定位函数起点。
3. 把 `line` 与 `code` 的第一行对齐——**校验只检查 `focus` 是否落在块内，不知道真实文件那一行长什么样**，这一段只能人核。
4. 复制时不要顺手格式化代码：行号会对不上。

## 用 AI 起草时要注意

AI 很适合起草"解释"与"小测题干"，但**不能信任它给的行号与结论**：

- 让它先读你贴进去的**原文件片段**，再写解释，别让它凭记忆。
- 行号一律自己核；`commit` 一律自己钉。
- 生成的 JSON 必须过一遍校验（管理端预览会直接告诉你哪一块坏了、坏在第几个字段）。
- 小测的正确答案要自己验一遍——AI 常把 `answer` 写成 1-based。

## 发布前检查

- [ ] 只讲了一个机制，5–12 步
- [ ] `commit` 是固定的，不是分支名
- [ ] 行号与 `code` 第一行对得上
- [ ] 至少一个 `predict`、一个 `state`
- [ ] 至少一道练习、三道小测
- [ ] 三个常见误解 + 面试追问
- [ ] 许可证已核，正文末尾有出处声明
- [ ] 管理端预览里五种块都正常渲染
- [ ] 学习者在窄屏（360px）下能读
