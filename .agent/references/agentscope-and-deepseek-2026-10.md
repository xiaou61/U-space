---
artifact: reference
status: active
updated: 2026-10-09
---

# AgentScope Java 与 DeepSeek 接入事实（2026-10-09 实测）

本文记录**实测与官方文档核实过**的接入事实，供 AI 相关工作项直接引用。未经核实的内容不写进来。

## 一、DeepSeek 侧（实测）

### 模型与能力

`GET https://api.deepseek.com/models` 返回的当前可用模型 id（2026-10-09）：

| 模型 id | 名称 | 上下文 | 最大输出 | 输入模态 |
| --- | --- | --- | --- | --- |
| `deepseek-flash` | DeepSeek-V4.1-Flash | 1,048,576 | 393,216 | 文本 + 图像 |
| `deepseek-v4-pro` | DeepSeek-V4-Pro | 1,048,576 | 393,216 | 文本 |

- `deepseek-chat` / `deepseek-reasoner` **已不是当前 id**；旧 id `deepseek-v4-flash` 仍被接受但路由到 V4.1-Flash。
- `deepseek-flash` 的 `effort` 支持 `low` / `high` / `max`，**默认 `high`**。
- `deepseek-flash` 是唯一支持图像输入的模型。
- 认证：`Authorization: Bearer $DEEPSEEK_API_KEY`；基址 `https://api.deepseek.com`，与 OpenAI 格式兼容（另有 `/anthropic` 兼容端点）。

### 实测结论

1. **思考模式默认开启，且推理 token 计入 `max_tokens`。** 用 `max_tokens: 100` 请求一句普通问答时，`completion_tokens` 为 100、其中 `reasoning_tokens` 为 100、**`content` 为空字符串**；同一请求把 `max_tokens` 提到 3000 后正常返回（推理 74 token，正文 46 字）。
   - **后果**：`max_tokens` 给小了会拿到"成功但内容为空"的响应。这不会报错，只会静默产出空结果。
   - **必须做的检查**：调用后校验内容非空；`max_tokens` 要给足推理开销（判分与出题这类任务建议不低于 2000）。
2. **`response_format: {"type": "json_object"}` 可用且有效。** 实测按"为知识点生成一道选择题，返回含 stem/options/answer_index/difficulty 的 json"的提示词请求，返回内容 `json.loads` 通过，四个字段齐全、内容合理（推理 271 token，正文 140 字）。
   - **限制**：DeepSeek 只支持 `json_object`，**不支持 `json_schema`，也没有 strict 模式**；官方指南要求提示词里出现 "json" 字样，并明确警告"可能偶尔返回空内容"。

## 二、AgentScope Java 侧（官方文档核实）

### 没有 DeepSeek 专用 starter

Maven Central 上 **不存在任何 `io.agentscope:*deepseek*` 构件**（目录列表、search.maven.org 查询、三个可能的构件名探测均为 404）。DeepSeek 由 **OpenAI 扩展**承载：

- `io.agentscope:agentscope-extensions-model-openai`（模型扩展）
- `io.agentscope:agentscope-openai-spring-boot-starter`（Spring Boot starter）

文档给出的接入方式是 `ModelRegistry` 的 id 前缀写法（非 Spring 路径）：

```java
ReActAgent agent = ReActAgent.builder()
    .name("assistant")
    .model("deepseek:deepseek-flash")
    .build();
```

该路径读取环境变量 `DEEPSEEK_API_KEY`，默认基址 `https://api.deepseek.com`，并把 `deepseek:` 前缀剥掉作为真实 model 名。**没有文档化的 `agentscope.deepseek.*` 配置命名空间**——用 Spring starter 时要按 OpenAI 提供方配置并显式设 `baseUrl("https://api.deepseek.com")` 与 `DeepSeekFormatter`。

### 结构化输出走回退路径

AgentScope 的提供方能力表明确写着：OpenAI 系（含 DeepSeek/GLM 用其 formatter）`supportsNativeStructuredOutput()` 为 **`false`**，标注"不支持，自动回退"。也就是说结构化输出由框架**注入一个合成工具、靠强制工具调用**带回数据，而不是走 provider 原生 schema。DeepSeek 页面结论一致。

### 一个会打到架构上的限制

DeepSeek 页面明确提示：当**同时**存在工具与结构化输出时，DeepSeek 会"优先满足 `response_format` 约束而跳过工具调用"，因此需要把 `nativeStructuredOutputWithTools(false)`。

**后果**：我们原先设想的「智能体一边调工具（查掌握度、前置图）一边返回结构化结果（学习路径）」这个组合，在 DeepSeek 上是**不成立的**。要么先调工具拿到数据、再用第二次调用产出结构化结果；要么学习路径的决策留在确定性代码里（这与之前的判断一致）。

### 思考模式的另一处影响

推理是同一模型上的开关（`enableThinking(true)`），流式输出会把 `ThinkingBlockDeltaEvent` 与 `TextBlockDeltaEvent` 分开推送。**前端的流式渲染必须能分别处理这两类事件**，否则思考内容与正式回答会混在一起。

## 三、ReActAgent 能不能用（2026-10-21 尖刺实测，**结论已改**）

问题反复被问起（公众号《AgentScope Java 事件流》传播后）。**第一版结论（"接不上、收益为零"）已被实测推翻**，下面是真实跑出来的结果。

### 尖刺工程

`C:\Users\Lenovo\AppData\Local\Temp\as-spike`（临时，不入库）：`agentscope-core:2.0.4` + `agentscope-extensions-model-openai:2.0.4` + JDK 25 + **真实 DeepSeek**，三个程序：

| 程序 | 验什么 | 结果 |
| --- | --- | --- |
| `Spike` | `streamEvents` 全事件 + **程序化 AgentTool**（非注解） | **通过**，5.9 秒跑完两轮工具调用 |
| `HookSpike` | `Hook` 能否拿到完整 prompt | **通过**，但 `hook` 包已标 `removal` |
| `MiddlewareSpike` | `MiddlewareBase` 能否落 payload 表要的全部字段 | **通过**，4.3 秒跑完 |

### 实测结论一：程序化工具直接能接，不需要注解、不需要挂起

这是第一版结论错得最狠的地方。`Toolkit.registerAgentTool(AgentTool)` 存在，而 `AgentTool` 的接口就是：

```java
String getName(); String getDescription(); Map<String,Object> getParameters();
Mono<ToolResultBlock> callAsync(ToolCallParam param);
```

**与项目自己的 `PaideiaTool` 形状几乎一致**，`callAsync` 里 `param.getInput()` 就是工具入参。既不用 `@Tool` 注解，也不用 Schema-Only + `TOOL_SUSPENDED` 的外部执行往返。

实测输出（第二轮才拿到工具结果，两轮共 2 次工具调用）：

```
TOOL_INPUT_LOOKUP={topic=redis}
TOOL_INPUT_MULTIPLY={factor=3, value=7}
ELAPSED_MS=5906
EVENT_COUNTS={AGENT_START=1, MODEL_CALL_START=3, TEXT_BLOCK_START=2, TEXT_BLOCK_DELTA=29,
  TEXT_BLOCK_END=2, TOOL_CALL_START=2, TOOL_CALL_DELTA=22, TOOL_CALL_END=2, MODEL_CALL_END=3,
  TOOL_RESULT_START=2, TOOL_RESULT_TEXT_DELTA=2, TOOL_RESULT_END=2, THINKING_BLOCK_START=1,
  THINKING_BLOCK_DELTA=8, THINKING_BLOCK_END=1, AGENT_RESULT=1, AGENT_END=1}
```

### 实测结论二：完整请求体拿得到，而且是在"发送前一刻"

`MiddlewareBase.onModelCall` 的入参 `ModelCallInput` 是 record，四个字段齐全：

```
MODEL_CALL#1 model=deepseek-flash msgs=2 msgChars=62
  roles=[SYSTEM:38, USER:24] tools=1 toolNames=[fetch_record]
MODEL_CALL#2 model=deepseek-flash msgs=4 msgChars=89
  roles=[SYSTEM:38, USER:24, ASSISTANT:12, TOOL:15] tools=1
```

`messages() / tools() / options() / model()` —— 这正是 WORK-030 `REQ-060/061` 要的"实际发送而不是打算发送"。

### 实测结论三：SDK 给的用量比我们现在记的多两种

```
MODEL_CALL_END#1 in=325 out=38 cached=0   reasoning=0   total=363 upstreamTime=2.523
MODEL_CALL_END#2 in=446 out=12 cached=256 reasoning=0   total=458 upstreamTime=0.874
```

**`cached=256` 是真实的缓存命中**。而项目现在的 `AgentScopeConversation.toUsage()` 只读 `inputTokens / outputTokens / totalTokens`，`step.tokens(..., 0)` 把 cacheRead **写死成 0** ⇒ `ai_runs.cache_read_tokens` 与 `ai_run_spans.cache_read_tokens` **永远是 0**，即使上游报了命中。`ChatUsage` 还另有 `getReasoningTokens()`，同样被丢掉。

**这是本次尖刺顺手挖出的一个真缺陷，与迁不迁移无关，应当单独修。**

### 实测结论四：`hook` 包已废弃，`Middleware` 是替代

`-Xlint:removal` 明确报：`io.agentscope.core.hook` 中的 `Hook` / `HookEvent` / `PreReasoningEvent` / `PostReasoningEvent`「已过时，且标记为待删除」。**新代码只用 `MiddlewareBase`。**

### 实测结论五：`ToolResultEndEvent` 不含结果正文

它的 accessor 只有 `getReplyId / getToolCallId / getToolCallName / getState`。要拿工具返回正文得靠：
- `PostActingEvent.getToolResult()`（但它在弃用的 hook 包里），或
- **在 `AgentTool.callAsync` 里自己记**（最直接，且我们本来就要在工具层做权限与审计）。

### 所以：迁移的代价重估

| 原判断 | 实测后 |
| --- | --- |
| 「动态工具接不上，要走挂起往返」 | **错**。`registerAgentTool` 直接支持，`find_tools` 也能实现成程序化工具 |
| 「收益为零，五类事件我们都有」 | **错**。SDK 额外给 `cached` / `reasoning` tokens、`FinalAnswerFilterMiddleware`、`OtelTracingMiddleware`、完整 `ModelCallInput` |
| 「记忆要写适配器」 | **仍然成立**。`AgentStateStore` 是 `(userId, sessionId, key)` KV，我们的记忆在 MySQL |
| 「草稿旁路要走 Middleware」 | **仍然成立**，但 Middleware 已验证可用 |
| 「DeepSeek 工具 + 结构化输出要重验」 | **已验**：`nativeStructuredOutputWithTools(false)` + `DeepSeekFormatter` 下工具循环正常 |

仍要自己写的只有三件：**记忆适配器**、**草稿旁路**（Middleware）、**工具层审计**（本来就有）。

### 建议路径

**值得做，但不并进 WORK-030。** 理由：WORK-030 是管理端信息密度，迁移会重写对话链路——两件事混在一个 WORK 里，任何一侧出问题都分不清是谁造成的。

先做**独立的一项**，且分两步落地：

1. **第一步（低风险、立刻有收益）**：升 `2.0.0 → 2.0.4`，**同时把 `backend/pom.xml` 的 MCP SDK pin 从 `0.17.0` 升到 `0.17.2`**（2.0.4 传递带的是 0.17.2，不升会 `NoSuchMethodError`）。这一步不改架构，白拿 `FinalAnswerFilterMiddleware` 与 `OtelTracingMiddleware`。
2. **第二步（换运行时）**：用 `ReActAgent` + `MiddlewareBase` 替换手写循环，把现有的实测补丁逐条对上——最终轮才推正文（交给 `FinalAnswerFilterMiddleware`）、每轮一条 `ToolResultMessage`（框架内处理）、思考不进历史（框架的 `ThinkingBlock` 处理）。**必须有对照套件**：同一批语料在旧循环与新 Agent 下逐条比对输出与用量。

### 什么情况下**不要**迁移

- 只为了"用上框架的更好写法"。第一步（升版本）已经能拿到大部分收益。
- 团队对 `Middleware` 的行为没有把握时——它包裹的是整条执行链，写错会让流式卡住。

### 第二、三步的六个尖刺（2026-10-21 追加，决定了实现方式）

六轮真实 DeepSeek 实测，得出的约束直接改变了原先设想：

| # | 验什么 | 结果 |
| --- | --- | --- |
| 4 | 运行中 `toolkit.registerAgentTool(...)` 能否让新工具在**同一请求的下一轮**可用 | **不能**。Toolkit 里确实有了该工具，但模型下一轮仍看不到，回答"调用通道没打开" |
| 5a | 用 `Middleware.onModelCall` 改写 `ModelCallInput.tools()` 注入 schema | **能看到**，但**执行失败**——execution 侧用的是请求开始时的快照 |
| 5b | 启动时注册**全部**工具 + Middleware 收窄/放开 schema | **成功**。`STARTUP_TOOLKIT=[get_weather, discover_tools]`，首轮 schema 只发 `discover_tools`，`find_tools` 之后下一轮发两个，`get_weather` 真被调用并返回数据 |
| 6 | 多条 `SYSTEM` 消息放进 `inputMessages` | **抛异常**：`Hooks must not inject SYSTEM messages into PreCallEvent.inputMessages. Use event.setSystemMessage() or event.appendSystemContent() instead` |
| 6b | 改用 `builder.sysPrompt(...)` | **正常**。框架把它作为唯一的 SYSTEM 放进每次模型调用（`call#1 count=2 [SYSTEM:50, USER:8]`） |
| 6c | 复用同一个 agent 实例连做两次调用（同一 sessionId） | **有状态泄漏**：`call#2 count=4 [SYSTEM:50, USER:8, ASSISTANT:495, USER:6]`——上一轮的助手回复被框架自己留在上下文里 |

**因此实现方式被这三条钉死：**

1. **每次请求新建 `ReActAgent`**（`try (ReActAgent agent = ...)`）。我们不能让框架管历史——历史在 MySQL，由 `ConversationWindow` 裁好传进来。复用实例会把历史发两遍。
2. **提示词必须合成一条 `sysPrompt`**。现在的 `toMessages()` 把条目上下文、记忆、学情**各挂一条 SYSTEM**（放用户侧会被模型当成用户说的话），这套必须改成"按顺序拼成一段文本"再交给 `sysPrompt`。
3. **工具在构建时全量注册，可见性由 Middleware 收窄**。`find_tools` 的"下一轮就能用"靠的是：这个 harness 能用的工具**已经全在执行侧**，Middleware 只负责决定这一轮**发给模型看**哪些。执行侧的权限校验仍在 `AgentToolBridge.run`（按 harness 视图判），与现在一致。

### 什么情况下**不要**迁移

- 只为了"用上框架的更好写法"。第一步（升版本）已经能拿到大部分收益。
- 团队对 `Middleware` 的行为没有把握时——它包裹的是整条执行链，写错会让流式卡住。

## 四、密钥与配置边界

- DeepSeek API 密钥存于本机 Windows 凭据管理器（`Codex/quick-server/paideia-deepseek`），**不进仓库、不写进任何被跟踪的文件**。
- 应用侧计划通过环境变量 `DEEPSEEK_API_KEY` 注入；本地开发配置放被 `.gitignore` 忽略的 `backend/config/application-local.yml`。
- 密钥曾在会话中以明文出现过，若认为会话记录敏感，应轮换。

## 来源

- DeepSeek 官方 API 文档：`https://api-docs.deepseek.com/api/create-chat-completion`、`https://api-docs.deepseek.com/quick_start/pricing`、`https://api-docs.deepseek.com/updates`、`https://api-docs.deepseek.com/guides/json_mode`
- AgentScope Java 文档：`https://java.agentscope.io/v2/en/docs/building-blocks/model`、`https://java.agentscope.io/v2/en/integration/model/deepseek`、`https://java.agentscope.io/v1/en/docs/task/model`
- Maven Central：`https://repo1.maven.org/maven2/io/agentscope/`
- 实测：`GET /models`、两次 `POST /chat/completions`（普通与 `json_object`），2026-10-09。
