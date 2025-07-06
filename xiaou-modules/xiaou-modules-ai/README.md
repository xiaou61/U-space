# xiaou-modules-ai

智慧校园 AI 模块（`xiaou-modules-ai`）集成了 Spring Boot、Spring AI、阿里云 DashScope LLM、向量检索 (RAG) 与工具调用能力，提供面向校园学习/生活/行政场景的智能对话与知识问答服务。

---
## 目录结构
```
aix/
├─ pom.xml                                  - Maven 依赖声明
├─ src/main/java/com/xiaou/ai/
│  ├─ app/                                  - 业务入口（CampusApp）
│  ├─ controller/                           - REST/SSE 对外接口
│  ├─ agent/                                - 通用智能体抽象与实现
│  ├─ advisor/                              - 自定义 ChatClient Advisors
│  ├─ rag/                                  - RAG 组件与配置
│  ├─ tools/                                - AI 可调用的工具实现
│  ├─ chatmemory/                           - ChatMemory 持久化示例
│  ├─ constant/                             - 常量
│  └─ readme.md (本文件位于模块根目录)
└─ src/main/resources/document/             - Markdown 知识库
```

---
## 文件作用说明（按包归类）

### app
| 文件 | 作用 |
| --- | --- |
| `CampusApp.java` | 核心业务类。「智慧校园助手」封装了 ChatClient，支持多轮对话、RAG、工具调用、流式 SSE、结构化输出等能力。 |

### controller
| 文件 | 作用 |
| --- | --- |
| `AiController.java` | 暴露 HTTP 接口（同步/流式/SSEEmitter）供前端调用；再向 `CampusApp`/`YuManus` 发起请求。 |

### agent
| 文件 | 作用 |
| --- | --- |
| `BaseAgent.java` | 智能体基类：管理状态机、执行循环（think/act/step）、SSE 流式输出。 |
| `ReActAgent.java` | ReAct 模式抽象，定义 `think`/`act` 方法。 |
| `ToolCallAgent.java` | ReAct 子类：实现 LLM 工具调用 (Tool Call) 逻辑，使用 `ToolCallingManager`。 |
| `YuManus.java` | 演示「超级智能体」：封装 `ToolCallAgent` + `ChatModel`，对外暴露 runStream。 |
| `model/AgentState.java` | 枚举：智能体状态 (IDLE/RUNNING/FINISHED/ERROR)。 |

### advisor
| 文件 | 作用 |
| --- | --- |
| `MyLoggerAdvisor.java` | 自定义 Advisor：打印单轮请求/回复日志。 |
| `ReReadingAdvisor.java` | 自定义 Prompt 改写 Advisor，提高推理能力。 |

### chatmemory
| 文件 | 作用 |
| --- | --- |
| `FileBasedChatMemory.java` | 基于文件的 ChatMemory 落地实现（示例，默认未启用）。 |

### constant
| 文件 | 作用 |
| --- | --- |
| `FileConstant.java` | 定义文件保存根目录常量。 |

### tools
| 文件 | 作用 |
| --- | --- |
| `FileOperationTool.java` | CRUD 本地文件。 |
| `PDFGenerationTool.java` | 根据文本生成 PDF，支持中文字体。 |
| `ResourceDownloadTool.java` | 下载远程资源到本地。 |
| `WebSearchTool.java` | 网络关键字搜索示例。 |
| `WebScrapingTool.java` | 抓取网页 HTML。 |
| `TerminalOperationTool.java` | 执行简单 shell 命令。 |
| `TerminateTool.java` | 终止智能体运行。 |
| `ToolRegistration.java` | 将上述工具收集为 Spring Bean (ToolCallback[])。 |

### rag
| 文件 | 作用 |
| --- | --- |
| `CampusDocumentLoader.java` | 读取 `resources/document/*.md`，提取元数据 `topic` 并转换成 Document 列表。 |
| `MyTokenTextSplitter.java` | 自定义 Token 切分示例。 |
| `MyKeywordEnricher.java` | 为文档补充关键词元信息。 |
| `CampusVectorStoreConfig.java` | 初始化内存向量库 `campusVectorStore` 并加载文档。 |
| `PgVectorVectorStoreConfig.java` | （可选）PGVector + PostgreSQL 向量库配置。 |
| `CampusRagCloudAdvisorConfig.java` | 阿里云知识库检索增强（云端 RAG）配置。 |
| `CampusContextualQueryAugmenterFactory.java` | 定义无检索结果时的兜底 Prompt。 |
| `CampusRagCustomAdvisorFactory.java` | 基于 `campusVectorStore` + 过滤条件 动态创建 RetrievalAugmentationAdvisor。 |
| `QueryRewriter.java` | 查询改写器：使用 LLM 改写用户查询，优化检索效果。 |

### document (resources)
| 文件 | 作用 |
| --- | --- |
| `校园常见问题和回答 - 学业篇.md` | 学业相关 FAQ，用于 RAG。 |
| `校园常见问题和回答 - 生活篇.md` | 校园生活 FAQ。 |
| `校园常见问题和回答 - 行政篇.md` | 行政/教务 FAQ。 |

### 其他
| 文件 | 作用 |
| --- | --- |
| `pom.xml` | 定义依赖：Spring Boot / Spring AI / DashScope SDK / hutool / iText / PGVector 等。 |

> **说明**：若新增/删除 Java 文件或资源，请同步更新本表。

---
## 调用流程详解

下面以最常见的 **同步 REST 接口** 为例，逐层剖析一次问答请求的完整链路，SSE 及流式调用仅在最后响应方式上有所区别。

1. **前端发起请求**  
   - URL：`GET /ai/campus_app/chat/sync`  
   - Query 参数：`message`（用户问题）、`chatId`（会话编号）

2. **`AiController` 接收并转调**  
   - Spring MVC 将请求映射到 `AiController#doChatWithCampusAppSync`。  
   - 方法内部直接调用 `CampusApp#doChat`。

3. **`CampusApp` 构建 Prompt & 组装 Advisor 链**  
   - 设置统一 **System Prompt**（智慧校园助手角色说明）。  
   - 创建/复用 `MessageWindowChatMemory` 并插入历史消息。  
   - 依次挂载默认 Advisors：
     1) `MessageChatMemoryAdvisor` —— 注入多轮上下文；
     2) `MyLoggerAdvisor` —— 记录请求/回复文本；
     3) *可选* `CampusRagCloudAdvisor` / `QuestionAnswerAdvisor` —— 执行 RAG 检索；
     4) *可选* `ToolCallbackProvider / ToolCallbacks` —— 启用工具调用。

4. **`ChatClient` 向 LLM 发送请求**  
   - 基于 DashScope Chat Model（阿里云百炼/灵积）完成一次推理。  
   - 如果 RAG Advisors 已挂载：
       * Query 将先经 `QueryRewriter` 改写；
       * 使用 `CampusVectorStore` 或云端知识库检索相关文档；
       * 检索到的内容以 **系统注入** 或 **上下文拼接** 形式参与推理。

5. **工具调用（可选）**  
   - 当模型判断需要外部能力时，返回 **ToolCall** 指令。  
   - `ToolCallingManager` 解析指令、按名称匹配对应 `ToolCallback`，执行 Java 方法：
       * 例如 `WebSearchTool#search` / `PDFGenerationTool#generatePDF` 等；  
       * 结果封装为 `ToolResponseMessage` 插入对话。  
   - ChatClient 再次向 LLM 发起 *follow-up* 推理，直至模型不再请求工具。

6. **生成最终回复**  
   - LLM 输出自然语言答案。  
   - `CampusApp` 取出 `chatResponse.getResult().getOutput().getText()`。

7. **返回 Controller**  
   - 同步接口：直接将字符串写入 HTTP 响应体；状态码 200。  
   - 流式/SSE 接口：`Flux<String>` 或 `SseEmitter` 按分块方式推送 token。

8. **前端展示**  
   - JSON/SSE 消息到达后，渲染到聊天窗口；若为流式，边到边渲染实现打字机效果。

> ⚠️  注：PGVector 配置、云知识库顾问、工具调用等均为可选组件，可通过注释/配置灵活开关。

---
## 快速测试
```bash
# 同步调用
GET /ai/campus_app/chat/sync?message=图书馆几点开门?&chatId=123

# SSE
GET /ai/campus_app/chat/sse?message=选课攻略&chatId=123
```

---
> 以上描述基于当前仓库代码自动生成，如有出入请以源代码为准。 