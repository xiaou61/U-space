# U-Space 2.X  (v2.2.0)

**U-Space 2.X** 是一套基于 Spring Boot 3 的"**智慧校园 +**"实践项目，目标简历一个庞大的智慧校园系统。

项目目前为V2.2.0正式版

## 项目部分截图

管理端

![image-20250713171327463](https://11-1305448902.cos.ap-chengdu.myqcloud.com/img/202507131713770.png)

教师端：

![image-20250713171451725](https://11-1305448902.cos.ap-chengdu.myqcloud.com/img/202507131714897.png)

用户端



![image-20250713171537392](https://11-1305448902.cos.ap-chengdu.myqcloud.com/img/202507131715680.png)

![image-20250713171551695](https://11-1305448902.cos.ap-chengdu.myqcloud.com/img/202507131715851.png)

![image-20250713171601494](https://11-1305448902.cos.ap-chengdu.myqcloud.com/img/202507131716606.png)

![image-20250713171612582](https://11-1305448902.cos.ap-chengdu.myqcloud.com/img/202507131716698.png)





## 技术栈

| 分层 | 关键组件 | 说明 |
|------|----------|------|
| 基础 | Java 17 · Spring Boot 3.4.4 | 项目核心运行环境 |
| 数据持久 | MyBatis-Plus 3.5 · MySQL | ORM + 分页插件 + 代码生成 |
| 缓存/消息 | Redis · Redisson · RabbitMQ | 高性能缓存 & 分布式锁 & MQ 异步 |
| 安全 | Sa-Token 1.44 | 登录、权限、会话管理 |
| 工具 & 通用 | Hutool, MapStruct-Plus, Lombok | 开发效率提升 |
| AI 能力 | Spring AI · DashScope SDK | LLM 调用、RAG、工具调用 |
| 监控 | Micrometer, Spring Actuator | 健康检查、指标采集 |
| 其他 | EasyExcel, Fastjson2, Lock4j | Excel 导入导出、JSON、分布式锁 |

---

## xiaou-common 模块功能概览

| 子模块 | 功能简述 |
|--------|---------|
| xiaou-common-core | 基础通用工具与常量、异常定义、分页对象等核心代码。 |
| xiaou-common-web | Spring Web 相关通用配置：全局异常处理、跨域、序列化策略等。 |
| xiaou-common-mybatis | MyBatis-Plus 自动填充、枚举处理、分页插件等持久层公共配置。 |
| xiaou-common-redis | 基于 Redisson 的 Redis 工具与自动配置。 |
| xiaou-common-excel | 通过 EasyExcel 导入导出，封装注解与转换器。 |
| xiaou-common-mail | 邮件发送封装（SMTP），支持模板与附件。 |
| xiaou-common-mq | 基于 Spring AMQP/RabbitMQ 的消息工具。 |
| xiaou-common-log | AOP 操作日志、注解 + 切面 + MQ 异步持久化。 |
| xiaou-common-satoken | 权限/登录统一封装（Sa-Token 组件）。 |
| xiaou-common-ratelimiter | 注解式限流（Redis + AOP），支持 IP/用户维度。 |
| xiaou-common-upload | 文件上传通用封装（本地 / OSS 可扩展）。 |
| xiaou-common-online | 在线用户 & 会话管理工具。 |
| xiaou-common-sse | Spring SSE 快速集成及工具封装。 |
| xiaou-common-bom | 统一版本管理 BOM，集中依赖版本号，供所有模块继承。 |

---
后续会继续补充其它模块的功能概览与整体架构说明。

## xiaou-modules 聚合模块

`xiaou-modules` 作为 **业务层** 目录，按照领域拆分多个微服务/子系统。

| 子模块                  | 功能概述                                                                  |
|----------------------|-----------------------------------------------------------------------|
| xiaou-modules-system | 后台系统管理：操作日志、公告管理、在线用户、文件上传等运维类功能。依赖 common-log / common-upload 等公共组件。 |
| xiaou-modules-ai     | 智慧校园 AI 助手：集成 DashScope LLM、RAG、工具调用，提供聊天、知识问答、PDF 生成等智能服务。           |
| xiaou-modules-auth   | 认证中心：登录 / 注册 / JWT 或 Sa-Token 鉴权、验证码、人机校验等身份安全功能。                     |
| xiaou-modules-study  | 学习管理：课程、作业、分组讨论等教学相关服务。                                               |
| xiaou-modules-index  | 首页功能编写                                                                |
| xiaou-modules-bbs    | 校园论坛                                                                  |

> 这些子模块均继承 `xiaou-modules/pom.xml` 聚合配置，统一版本号与依赖。

---
