# 🎓 U-space 高校一体化服务平台

U-space 是一个综合性的高校管理平台，致力于整合校园中各类常用功能，构建一个 **功能齐全、体验统一** 的一站式大学服务中心，全面提升师生在教学、生活、服务等方面的效率与体验。

> 🛠 当前版本：`v1.1.0`  
> 📄 文档地址：[https://uspacevite.xiaou61.xyz/](https://uspacevite.xiaou61.xyz/)

---

## 🌟 项目简介

U-space 并非一个简单的 CRUD 项目，而是经过多轮迭代优化、融合多种现代开发理念与企业级架构实践的综合性平台。

由于复杂的业务逻辑因此适配不想只写CRUD项目，体验真实开发逻辑的用户去学习。

主要特性包括：

- 🌐 校园信息门户：公告、新闻、课表、校历等
- 🧭 智能导航：校园地图、建筑导航（前身模块）
- 📝 在线服务：报修、请假、审批、问卷
- 🎓 教务功能：选课、成绩、考勤、课表管理
- 📱 通知提醒：站内信、点赞评论、系统提醒
- 💬 通用互动：论坛、评论、点赞、收藏、话题
- 🧩 插件架构：支持扩展模块动态接入
- 👨‍🏫 多角色支持：学生、教师、管理员权限隔离

---

## 🏗 技术架构

U-space 后端基于 **Spring Boot 3 + JDK 17** 构建，模块清晰、架构现代，满足大型系统的性能和扩展性需求。

| 分类       | 技术选型                                                     |
| ---------- | ------------------------------------------------------------ |
| 核心框架   | Spring Boot 3.4.4、Spring Context、Spring Validation         |
| 安全认证   | [Sa-Token](https://sa-token.cc/) + Redis（权限缓存与业务缓存分离） |
| 持久化层   | MyBatis-Plus 3.5.11、JSqlParser、P6Spy SQL 性能分析          |
| 接口文档   | SpringDoc OpenAPI 2.8.5（Swagger UI）                        |
| 缓存中间件 | Redis、Redisson、Lock4j 分布式锁                             |
| 工具集     | Hutool、Lombok、MapStruct-Plus、Fastjson2、Jackson           |
| 文件存储   | X-File-Storage（支持多云平台策略上传）                       |
| 模块结构   | 多模块：xiaou-admin / xiaou-common / xiaou-modules           |
| 构建工具   | Maven + Flatten Plugin + Compiler Plugin                     |

> 所有依赖版本集中在父模块中统一管理，项目具备良好的可维护性与长期演进能力。

---

## 🧩 模块概览

- **xiaou-admin**：项目启动器，聚合 API 入口，核心配置集中地
- **xiaou-common**：通用工具类封装，如响应结构、异常、分页封装等
- **xiaou-modules**：业务模块合集，如用户、课程、通知、论坛等

模块划分遵循 **“高内聚、低耦合”** 原则，每个模块可独立演化，便于拆分部署或微服务改造。

---

## 🚀 快速启动

### 环境要求

- JDK 17+
- Maven 3.6+
- Redis 服务（本地或远程）
- 数据库：MySQL 8+

### 克隆项目

```bash
git clone https://github.com/xiaou61/U-space.git
cd U-space
```

### 数据库准备

导入 `sql/v1.1.0.sql` 初始化表结构与基础数据。

## 📄 License

本项目遵循 MIT License 开源许可证：

> 你可以**自由地使用、复制、修改、合并、发布、分发**本软件及其副本，也可以将其用于**商业用途**，前提是在软件中保留原始许可证声明。

**简要说明：**

- ✅ 允许用于个人或商业项目
- ✅ 允许修改和二次发布
- ✅ 可集成进闭源项目
- ❗ 需保留原始作者版权信息和许可证文件（LICENSE）

> 如果你基于本项目开发出了更好的产品或功能，欢迎通过 Pull Request 贡献回社区 🙌

## 📌 TODO 与规划

[Docs (feishu.cn)](https://yxy7auidhk0.feishu.cn/docx/N2jwdTaOMoIarDx1BwEcUdjBnoh)