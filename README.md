# Code Nest 


![Version](https://img.shields.io/badge/version-1.1.0-blue.svg)
![Java](https://img.shields.io/badge/java-17-orange.svg)
![Spring Boot](https://img.shields.io/badge/spring%20boot-3.4.4-brightgreen.svg)
![Vue](https://img.shields.io/badge/vue-3.x-4fc08d.svg)
![License](https://img.shields.io/badge/license-MIT-green.svg)



## 📖 项目简介

Code Nest 是一个基于 Spring Boot + Vue3 的前后端分离的一个程序员社区系统。

### ✨ 主要特性

- 🔐 **完善的权限管理** - JWT token认证，支持管理员和普通用户双重权限体系
- 📚 **丰富的题库功能** - 支持题目分类、题单管理、随机抽题、收藏功能
- 🎯 **智能抽题系统** - 可选择多个题单进行随机抽题，支持自定义抽题数量
- 💫 **现代化UI** - 基于Element Plus的美观界面，支持响应式设计
- 📊 **系统监控** - SQL监控、操作日志、登录日志等完整的监控体系
- 🚀 **高性能架构** - Redis缓存、数据库连接池、异步处理等性能优化

## 🏗️ 技术架构

### 后端技术栈

- **框架**: Spring Boot 3.4.4
- **安全**: Spring Security + JWT
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
    │    │   xiaou-system  │   │
    │    │   xiaou-user    │   │
    │    │ xiaou-interview │   │
    │    │  xiaou-monitor  │   │
    │    │  xiaou-common   │   │
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
mysql -u root -p code_nest < sql/struct.sql
mysql -u root -p code_nest < sql/data.sql
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
java -jar target/xiaou-application-1.1.0.jar
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

## 📱 功能特性

### 管理员功能

- **🔐 系统管理**
  - 管理员登录/登出
  - 个人资料管理
  - 密码修改

- **👥 用户管理**
  - 用户列表查看
  - 用户状态管理
  - 用户信息编辑

- **📚 题库管理**
  - 题目分类管理
  - 题目CRUD操作
  - 题单管理
  - 批量导入导出

- **📊 系统监控**
  - SQL执行监控
  - 慢查询分析
  - 操作日志
  - 登录日志

### 用户功能

- **🔐 账户管理**
  - 用户注册/登录
  - 个人中心
  - 密码修改

- **📖 题库浏览**
  - 分类浏览题目
  - 题单查看
  - 题目搜索

- **🎯 学习功能**
  - 随机抽题练习
  - 题目收藏
  - 学习记录

- **💡 智能功能**
  - 多题单组合抽题
  - 自定义抽题数量
  - 收藏夹管理

## 🔧 配置说明

### JWT配置

```yaml
jwt:
  secret: your-secret-key
  expiration: 604800        # 7天 (秒)
  refresh-expiration: 2592000  # 30天 (秒)
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
docker build -t code-nest:1.1.0 .

# 运行容器
docker run -d \
  --name code-nest \
  -p 9999:9999 \
  -e SPRING_PROFILES_ACTIVE=prod \
  code-nest:1.1.0
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

### v1.1.0 (2025-09-05)

#### 🚀 功能增强
- 新增社区模块
- 社区帖子管理
- 评论系统
- 用户互动功能

#### 🔧 技术改进
- 优化系统架构
- 代码结构重构
- 性能优化

### v1.0.0 (2025-09-03)

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

## 预计新增功能

- 社区功能
- 开发者工具功能