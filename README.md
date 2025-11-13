# Code Nest 


![Version](https://img.shields.io/badge/version-1.6.0-blue.svg)
![Java](https://img.shields.io/badge/java-17-orange.svg)
![Spring Boot](https://img.shields.io/badge/spring%20boot-3.4.4-brightgreen.svg)
![Vue](https://img.shields.io/badge/vue-3.x-4fc08d.svg)
![License](https://img.shields.io/badge/license-MIT-green.svg)



## 📖 项目简介

Code Nest 是一个基于 Spring Boot + Vue3 的前后端分离的一个程序员社区系统。

### ✨ 主要特性

- 🔐 **完善的权限管理** - Sa-Token认证系统，支持管理员和普通用户双重权限体系
- 📚 **丰富的题库功能** - 支持题目分类、题单管理、随机抽题、收藏功能
- 🎯 **智能抽题系统** - 可选择多个题单进行随机抽题，支持自定义抽题数量
- 💫 **现代化UI** - 基于Element Plus的美观界面，支持响应式设计
- 📊 **系统监控** - SQL监控、操作日志、登录日志等完整的监控体系
- 🚀 **高性能架构** - Redis缓存、数据库连接池、异步处理等性能优化

## 🏗️ 技术架构

### 后端技术栈

- **框架**: Spring Boot 3.4.4
- **安全**: Sa-Token权限认证框架
- **数据库**: MySQL 8.0 + MyBatis
- **缓存**: Redis (Redisson)
- **文档**: SpringDoc (OpenAPI 3.0)
- **工具**: Hutool、Lombok
- **监控**: 自研SQL监控系统

### 前端技术栈

- **框架**: Vue 3 + Composition API
- **构建工具**: Vite
- **UI组件**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **HTTP客户端**: Axios
- **样式**: SCSS

### 系统架构

```
┌─────────────────┐    ┌─────────────────┐
│   管理员端       │    │     用户端       │
│  (vue3-admin)   │    │  (vue3-user)    │
└─────┬───────────┘    └─────┬───────────┘
      │                      │
      └──────────┬───────────┘
                 │
    ┌────────────▼─────────────┐
    │      Nginx (可选)        │
    └────────────┬─────────────┘
                 │
    ┌────────────▼─────────────┐
    │     Spring Boot API      │
    │    ┌─────────────────┐   │
    │    │   xiaou-system    │   │
    │    │   xiaou-user      │   │
    │    │ xiaou-interview   │   │
    │    │  xiaou-monitor    │   │
    │    │  xiaou-moment     │   │
    │    │ xiaou-notification│   │
    │    │ xiaou-filestorage │   │
    │    │  xiaou-knowledge  │   │
    │    │   xiaou-version   │   │
    │    │    xiaou-moyu     │   │
    │    │    xiaou-points   │   │
    │    │     xiaou-chat    │   │
    │    │  xiaou-common     │   │
    │    └─────────────────┘   │
    └────────────┬─────────────┘
                 │
    ┌────────────▼─────────────┐
    │      MySQL + Redis       │
    └──────────────────────────┘
```

## 📦 项目结构

```
Code-Nest/
├── docs/                           # 文档目录
├── sql/                           # 数据库脚本
│   ├── struct.sql                 # 表结构
│   └── data.sql                   # 初始数据
├── vue3-admin-front/              # 管理员前端
├── vue3-user-front/               # 用户端前端
├── xiaou-application/             # 启动模块
├── xiaou-common/                  # 公共模块
│   ├── annotation/                # 自定义注解
│   ├── aspect/                    # 切面处理
│   ├── config/                    # 配置类
│   ├── core/                      # 核心类
│   ├── enums/                     # 枚举类
│   ├── exception/                 # 异常处理
│   ├── security/                  # 安全相关
│   └── utils/                     # 工具类
├── xiaou-interview/               # 面试题模块
│   ├── controller/                # 控制器
│   │   ├── admin/                 # 管理员接口
│   │   └── pub/                   # 公开接口
│   ├── domain/                    # 实体类
│   ├── dto/                       # 数据传输对象
│   ├── mapper/                    # 数据映射
│   └── service/                   # 业务逻辑
├── xiaou-monitor/                 # 监控模块
├── xiaou-system/                  # 系统模块
├── xiaou-user/                    # 用户模块
├── xiaou-moment/                  # 朋友圈模块
├── xiaou-notification/            # 消息通知模块
├── xiaou-filestorage/             # 文件存储模块
├── xiaou-knowledge/               # 知识图谱模块
├── xiaou-version/                 # 版本管理模块
├── xiaou-moyu/                    # 摸鱼工具模块
├── xiaou-points/                  # 积分系统模块
├── xiaou-chat/                    # IM聊天室模块
└── pom.xml                        # Maven主配置
```

## 🚀 Quick Start

### 环境要求

- Java 17+
- Node.js 16+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.8+

### 1. 克隆项目

```bash
git clone https://github.com/your-username/Code-Nest.git
cd Code-Nest
```

### 2. 数据库初始化

```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE code_nest DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入表结构和数据

```

### 3. 配置文件

修改 `xiaou-application/src/main/resources/application-dev.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/code_nest?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8
    username: your_username
    password: your_password
  data:
    redis:
      redisson:
        config: |
          singleServerConfig:
            address: "redis://127.0.0.1:6379"
            database: 3
```

### 4. 启动后端

```bash
# 编译项目
mvn clean package -DskipTests

# 启动服务
cd xiaou-application
mvn spring-boot:run

# 或者运行jar包
java -jar target/xiaou-application-1.6.0.jar
```

服务启动后访问: http://localhost:9999/api

API文档: http://localhost:9999/api/swagger-ui.html

### 5. 启动前端

#### 管理员端

```bash
cd vue3-admin-front
npm install
npm run dev
```

访问: http://localhost:5173

默认管理员账号: `admin` / `123456`

#### 用户端

```bash
cd vue3-user-front
npm install
npm run dev
```

访问: http://localhost:5174

可注册新用户或使用测试账号

### 6. 构建部署

```bash
# 后端打包
mvn clean package -DskipTests

# 前端打包
cd vue3-admin-front && npm run build
cd vue3-user-front && npm run build
```

## 🔧 配置说明

### Sa-Token配置

```yaml
sa-token:
  token-name: Authorization
  timeout: 604800           # Token有效期 7天 (秒)
  activity-timeout: 1800    # Token临时有效期 30分钟 (秒)
  is-concurrent: true       # 允许同一账号并发登录
  is-share: false          # 每次登录新建Token
  token-style: uuid        # Token风格
  token-prefix: Bearer     # Token前缀
  
  # Sa-Token 独立 Redis 配置
  alone-redis:
    host: 127.0.0.1
    port: 6379
    database: 4            # 使用独立database
```

### Redis配置

```yaml
spring:
  data:
    redis:
      redisson:
        config: |
          singleServerConfig:
            address: "redis://127.0.0.1:6379"
            database: 3
```

### 监控配置

```yaml
xiaou:
  monitor:
    enabled: true                    # 启用SQL监控
    slow-sql-threshold: 1000        # 慢SQL阈值(ms)
    async-save: true                # 异步保存日志
    retention-days: 30              # 日志保留天数
```

## 🚀 部署指南

### Docker部署

```bash
# 构建镜像
docker build -t code-nest:1.6.0 .

# 运行容器
docker run -d \
  --name code-nest \
  -p 9999:9999 \
  -e SPRING_PROFILES_ACTIVE=prod \
  code-nest:1.6.0
```

### Nginx配置

```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    # 前端静态文件
    location / {
        root /usr/share/nginx/html;
        try_files $uri $uri/ /index.html;
    }
    
    # API代理
    location /api/ {
        proxy_pass http://localhost:9999/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 🤝 贡献指南

欢迎贡献代码！请遵循以下步骤:

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 📄 更新日志

### v1.6.0 🚀

#### 🆕 新增内容
- **在线简历模块**：新增 xiaou-resume 服务，提供模板中心、简历管理、模块化内容、版本快照、分享与访问统计。
- **代码共享中心**：CodePen 模块正式上线（前后台 + SQL），支持作品发布、Fork、收藏、评论与后台运营。
- **监控体系升级**：接入 Prometheus + Grafana，结合 Actuator / Micrometer 指标与告警规则，替换旧 SQL 调用树监控。
- **敏感词增强**：敏感词系统补充统计分析与配置项，管理端可视化能力同步完善。
- **数据库脚本**：sql/v1.6.0 目录新增简历与 CodePen 建表脚本，升级一步到位。

#### 🔧 优化与修复
- 简历导出支持 PDF / Word / HTML，并与文件存储打通，自动上传至 COS 返回真实下载链接。
- 用户端上线模板大厅、简历工作台与编辑器；管理端提供模板维护、数据总览、健康巡检页面。
- README、导航、版本号等展示信息同步到 1.6.0，导出/分享交互体验全面升级。

### v1.5.0 🎉

#### 📝 博客系统模块
- **博客功能完整实现** - 全新的博客创作和管理系统
  - 博客文章发布和管理
  - Markdown编辑器支持
  - 文章分类和标签
  - 文章搜索和筛选
  - 评论互动功能

#### 🎲 抽奖系统模块
- **抽奖管理功能** - 完整的抽奖活动管理
  - 抽奖活动创建和配置
  - 奖品设置和库存管理
  - 中奖记录和统计
  - 后台抽奖管理界面

#### 🏷️ 社区增强功能
- **标签管理系统** - 社区内容分类和组织
  - 标签创建和管理
  - 标签关联和搜索
  - 热门标签统计
  - 标签云展示

#### ✨ 动态功能增强
- **动态收藏和搜索** - 丰富的动态互动功能
  - 动态收藏功能
  - 动态搜索和筛选
  - 用户动态列表
  - 个人收藏列表
  
- **热门动态算法** - 智能推荐系统
  - 热度分数计算（点赞×2 + 评论×3 + 浏览×0.1）
  - 定时任务更新热门动态（每10分钟）
  - 动态浏览统计和防重复记录
  - 批量获取优化性能
  
- **动态安全机制** - 内容审核和过滤
  - 敏感词检测和过滤
  - 内容安全性提升
  - 违规内容拦截

#### 🛡️ 敏感词系统增强
- **统计分析功能** - 敏感词监控和分析
  - 敏感词命中统计
  - 数据分析报表
  - 趋势分析和预警
  - 过滤效果评估

#### 📊 监控系统重构
- **Prometheus监控集成** - 现代化监控方案
  - 集成Prometheus和Grafana
  - Spring Boot Actuator配置
  - Micrometer Prometheus Registry
  - JVM、HTTP、数据库连接池监控
  - 预定义告警规则
  - 完整的监控部署文档
  - 移除旧的SQL调用树监控模块

#### 🔧 认证系统优化
- **Token管理优化** - 更好的用户体验
  - 优化Token过期处理逻辑
  - 改进Token续签机制
  - 统一Sa-Token权限认证
  - 管理员权限验证增强

#### 📦 数据库更新
- 新增博客相关表（blog_posts、blog_categories、blog_tags等）
- 新增抽奖系统表（lottery_activities、lottery_prizes、lottery_records）
- 新增社区标签表（community_tags）
- 新增动态收藏表（moment_favorites、moment_views）
- 新增敏感词统计表优化

### v1.4.0 🚀

#### 🔐 权限系统重构
- **Sa-Token认证系统迁移** - 从JWT迁移到Sa-Token
  - 使用Sa-Token独立Redis存储（database 4）
  - 支持管理员和用户双账号体系（StpAdminUtil / StpUserUtil）
  - 完善的Session管理和Token续签机制
  - 路由级权限拦截配置
  - 优化的异常处理（401/403状态码）
  - WebSocket Token认证支持

#### 💬 IM聊天室模块
- **实时聊天功能** - 基于WebSocket的即时通讯
  - WebSocket实时消息推送
  - 支持文本、图片、系统消息
  - 在线用户列表和人数统计
  - 历史消息加载（分页）
  - 消息撤回功能（2分钟内）
  - 心跳检测和断线重连
  - 管理员功能：
    - 消息管理和删除
    - 用户踢出功能
    - 系统公告推送
    - 在线用户监控

#### ⭐ 积分系统模块
- **用户积分管理** - 完整的积分体系
  - 积分规则配置和管理
  - 积分明细记录和查询
  - 用户积分统计
  - 积分排行榜
  - 支持多种积分变动类型
  - 积分日志追踪

#### 🔧 技术架构优化
- **全局异常处理增强**
  - Sa-Token异常统一处理
  - NotLoginException → 401 状态码
  - NotPermissionException → 403 状态码
  - 详细的异常类型区分和消息提示

- **WebSocket安全认证**
  - Sa-Token握手拦截器
  - Token验证和用户信息提取
  - Session数据同步到WebSocket attributes

- **模块化架构**
  - xiaou-chat 聊天室模块
  - xiaou-points 积分系统模块
  - 模块间依赖优化

#### 📦 数据库更新
- 新增聊天室相关表（chat_rooms、chat_messages、chat_user_bans、chat_online_users）
- 新增积分系统表（points_rules、user_points、points_detail）

### v1.3.0 🔥

#### 🚀 重大功能新增
- **🤖 Coze AI工作流集成** - 引入智能AI工作流平台
  - 支持Coze平台工作流调用
  - 提供统一工具类和配置管理
  - 同步和异步两种调用方式
  - 参数传递、结果缓存、异常处理等完整功能
  - 通过枚举统一管理工作流信息

- **🐛 Bug商店功能模块** - 趣味化编程体验
  - Bug条目展示和管理系统
  - 随机Bug获取和浏览功能
  - 用户Bug浏览历史记录
  - Bug内容复制和切换浏览
  - 批量导入和数据初始化
  - 管理端完整的CRUD操作

- **🎮 摸鱼工具集** - 程序员专属工具箱
  - 程序员日历和每日内容管理
  - 时薪计算器功能
  - 今日热榜数据展示
  - 热榜数据初始化和管理

- **🛠️ 开发者工具模块** - 提升开发效率
  - 程序员工具模块集成
  - 完善的工具路由配置
  - 统一的工具管理界面

#### 🎨 用户体验优化
- **导航系统升级** - 全局导航栏设计
  - 统一的页面布局优化
  - 响应式导航交互
  - 更好的用户体验流程

- **版本管理功能** - 系统版本追踪
  - 版本历史管理和展示
  - 菜单搜索功能实现
  - 版本信息统一管理

#### 🔧 技术架构优化
- **知识图谱增强** - 知识管理系统完善
  - 知识节点内容改为飞书文档链接
  - 管理员端知识图谱和节点管理
  - 更好的知识内容组织方式

- **模块化架构** - 新增多个功能模块
  - xiaou-moyu 摸鱼工具模块
  - xiaou-version 版本管理模块
  - 模块间依赖关系优化

### v1.2.1

#### 🚀 新增功能
- **敏感词过滤系统** - 完善的内容审核机制
  - 支持敏感词分类管理
  - 多种过滤策略配置
  - 敏感词检测日志记录
  - 实时内容过滤与替换

#### 🔧 系统优化
- 优化通知模块性能和功能
- 完善各模块间的集成和协作
- 提升系统稳定性和可维护性

### v1.2.0

#### 🚀 新增功能模块
- **朋友圈模块** - 全新的社交功能
  - 用户动态发布与展示
  - 点赞、评论、转发功能
  - 动态可见性控制
  - 图片/视频多媒体支持
  
- **消息通知模块** - 完整的消息推送系统
  - 系统消息通知
  - 用户互动消息
  - 消息状态管理
  - 批量操作支持
  
- **文件存储模块** - 统一的文件管理系统
  - 多种存储策略支持(本地、云存储)
  - 文件上传下载管理
  - 文件访问权限控制
  - 存储统计与监控

#### 🔧 技术改进
- 完善模块化架构设计
- 优化系统安全性
- 提升用户体验

### v1.1.1 

#### 🔧 技术改进
- 重构SQL监控系统，优化为监控树类型结构
- 提升监控数据可视化展示效果
- 优化监控性能和用户体验

### v1.1.0

#### 🚀 功能增强
- 新增社区模块
- 社区帖子管理
- 评论系统
- 用户互动功能

#### 🔧 技术改进
- 优化系统架构
- 代码结构重构
- 性能优化

### v1.0.0

#### 🎉 首次发布

**核心功能**
- 后台管理
- 面试题管理

## 📞 联系方式

- 项目维护者: xiaou
- 邮箱: 3153566913@qq.com
- 项目地址: https://github.com/xiaou61/Code-Nest

## 📜 开源协议

本项目基于 [MIT License](LICENSE) 开源协议。

**⭐ 如果这个项目对你有帮助，请点个Star支持一下！⭐**

## 项目功能截图

### 管理员端

![image-20251001192022408](https://11-1305448902.cos.ap-chengdu.myqcloud.com/imgs/202510011920552.png)

![image-20251001192046724](https://11-1305448902.cos.ap-chengdu.myqcloud.com/imgs/202510011920794.png)

![image-20251001192101335](https://11-1305448902.cos.ap-chengdu.myqcloud.com/imgs/202510011921406.png)

![image-20251001192116123](https://11-1305448902.cos.ap-chengdu.myqcloud.com/imgs/202510011921200.png)

![image-20251001192148551](https://11-1305448902.cos.ap-chengdu.myqcloud.com/imgs/202510011921665.png)

### 用户端

![image-20251001192200120](https://11-1305448902.cos.ap-chengdu.myqcloud.com/imgs/202510011922393.png)

![image-20251001192206833](https://11-1305448902.cos.ap-chengdu.myqcloud.com/imgs/202510011922904.png)

![image-20251001192254045](https://11-1305448902.cos.ap-chengdu.myqcloud.com/imgs/202510011922111.png)

![image-20251001192300510](https://11-1305448902.cos.ap-chengdu.myqcloud.com/imgs/202510011923575.png)

![image-20251001192306591](https://11-1305448902.cos.ap-chengdu.myqcloud.com/imgs/202510011923659.png)

![image-20251001192312505](https://11-1305448902.cos.ap-chengdu.myqcloud.com/imgs/202510011923749.png)

![image-20251001192320125](https://11-1305448902.cos.ap-chengdu.myqcloud.com/imgs/202510011923340.png)

![image-20251001192329371](https://11-1305448902.cos.ap-chengdu.myqcloud.com/imgs/202510011923495.png)

