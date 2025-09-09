# 朋友圈模块PRD v1.0.0

## 1. 项目基本信息

**项目名称：** Web朋友圈（简版动态广场）  
**版本：** v1.0.0  
**技术栈：** Spring Boot + MyBatis + Vue3  
**适用端：** PC端（优先 

## 2. 背景与目标

### 2.1 背景
平台需要一个轻量级的用户互动区，支持用户发动态、看动态、互动（点赞、评论）。类似朋友圈，但去掉好友关系，只保留核心功能，营造轻社交氛围。

### 2.2 产品目标
- **核心目标：** 提供简洁的动态发布与浏览功能
- **用户目标：** 用户可以便捷地分享生活动态，与其他用户互动
- **业务目标：** 提升用户活跃度和粘性，增加平台社交属性

## 3. 功能需求

### 3.1 动态管理

#### 3.1.1 发布动态
**基础功能：**
- 文本输入：支持100字以内文本
- 表情支持：内置emoji表情包
- 图片上传：最多9张图片，支持jpg/png/gif格式
- 发布权限：仅登录用户可发布

**交互细节：**
- 文本计数器实时显示剩余字数
- 图片支持拖拽上传和点击上传
- 图片预览功能，支持删除已选图片
- 发布前内容校验

**限制规则：**
- 文本：1-100字符
- 图片：单张最大5MB，总数最多9张
- 发布频率：同一用户5分钟内最多发布3条动态

#### 3.1.2 动态展示
**显示内容：**
- 用户信息：头像、昵称
- 发布时间：相对时间显示（如"3分钟前"）
- 动态内容：文本、表情、图片九宫格
- 互动数据：点赞数、评论数
- 操作按钮：点赞、评论、删除（仅自己的动态）

**排序规则：**
- 按发布时间倒序排列
- 支持分页加载（每页20条）

#### 3.1.3 动态删除
- 用户只能删除自己发布的动态
- 删除需二次确认
- 删除后相关点赞、评论数据一并删除

### 3.2 点赞功能

#### 3.2.1 点赞操作
- 登录用户可对任意动态进行点赞/取消点赞
- 同一用户对同一动态只能点赞一次
- 点赞状态实时更新，支持乐观锁

#### 3.2.2 点赞展示
- 显示点赞总数
- 显示当前用户是否已点赞（高亮状态）
- 点击查看点赞用户列表（显示前10个用户头像）

### 3.3 评论功能

#### 3.3.1 评论发表
- 支持文字评论（最多200字）
- 支持emoji表情
- 单层评论结构（不支持回复评论）

#### 3.3.2 评论展示
- 显示评论者头像、昵称、时间
- 默认显示最新3条评论
- 支持展开查看全部评论
- 评论按时间正序排列

#### 3.3.3 评论管理
- 用户可删除自己的评论
- 动态发布者可删除自己动态下的任意评论

## 4. 非功能需求

### 4.1 性能要求
- 动态列表加载时间 < 2秒
- 图片上传响应时间 < 5秒
- 支持并发用户数 > 1000

### 4.2 安全要求
- 内容审核：敏感词过滤
- 图片安全：病毒扫描、格式验证
- 防刷保护：发布频率限制、IP限制

### 4.3 兼容性要求
- PC端：Chrome 80+、Firefox 75+、Safari 13+
- 移动端：iOS 12+、Android 8+

## 5. 页面设计

### 5.1 动态广场页面（用户端）

#### 5.1.1 页面布局
```
┌─────────────────────────────────────────┐
│ 导航栏                                     │
├─────────────────────────────────────────┤
│ 发布动态区域                               │
│ ┌─────────┐ 想说点什么... [发布]          │
│ │ 头像    │ [📷] [😊]                      │
│ └─────────┘                              │
├─────────────────────────────────────────┤
│ 动态列表                                   │
│ ┌─────────┐ 用户A · 2小时前               │
│ │ 头像    │ 今天天气真好啊~ [图片]         │
│ └─────────┘ 👍 12  💬 5                    │
│             └─ 用户B：是的呢              │
│             └─ 用户C：+1                  │
├─────────────────────────────────────────┤
│ [加载更多]                                │
└─────────────────────────────────────────┘
```

#### 5.1.2 交互规范
- 发布区域：点击文本框自动展开，显示字数统计
- 图片上传：支持拖拽和点击上传，显示进度条
- 动态卡片：hover效果，操作按钮高亮
- 无限滚动：滚动到底部自动加载更多

### 5.2 管理后台页面（管理端）

#### 5.2.1 动态管理
- 动态列表：分页展示所有动态
- 批量操作：批量删除违规动态
- 内容审核：敏感词标记、人工审核

#### 5.2.2 统计分析
- 发布统计：每日发布数量趋势
- 用户活跃度：发布用户排行
- 互动统计：点赞评论数据分析

## 6. 技术方案

### 6.1 数据库设计

#### 6.1.1 动态表（moments）
```sql
CREATE TABLE moments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '动态ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    content TEXT COMMENT '动态内容',
    images TEXT COMMENT '图片URLs，JSON格式',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    comment_count INT DEFAULT 0 COMMENT '评论数',
    status TINYINT DEFAULT 1 COMMENT '状态：1正常 0删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time),
    INDEX idx_status (status)
) COMMENT '动态表';
```

#### 6.1.2 点赞表（moment_likes）
```sql
CREATE TABLE moment_likes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '点赞ID',
    moment_id BIGINT NOT NULL COMMENT '动态ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_moment_user (moment_id, user_id),
    INDEX idx_moment_id (moment_id),
    INDEX idx_user_id (user_id)
) COMMENT '动态点赞表';
```

#### 6.1.3 评论表（moment_comments）
```sql
CREATE TABLE moment_comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论ID',
    moment_id BIGINT NOT NULL COMMENT '动态ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    content TEXT NOT NULL COMMENT '评论内容',
    status TINYINT DEFAULT 1 COMMENT '状态：1正常 0删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_moment_id (moment_id),
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) COMMENT '动态评论表';
```

### 6.2 接口设计

#### 6.2.1 动态相关接口
```
POST /api/moments/publish              # 发布动态
GET  /api/moments/list                 # 获取动态列表
DELETE /api/moments/{id}               # 删除动态
POST /api/moments/{id}/like            # 点赞/取消点赞
POST /api/moments/{id}/comment         # 发表评论
GET  /api/moments/{id}/comments        # 获取评论列表
DELETE /api/moments/comments/{id}      # 删除评论
```

#### 6.2.2 请求/响应示例
**发布动态：**
```json
// 请求
POST /api/moments/publish
{
    "content": "今天天气真好！",
    "images": ["url1", "url2"]
}

// 响应
{
    "code": 200,
    "message": "发布成功",
    "data": {
        "momentId": 123
    }
}
```

**获取动态列表：**
```json
// 请求
GET /api/moments/list?page=1&size=20

// 响应
{
    "code": 200,
    "message": "获取成功",
    "data": {
        "records": [
            {
                "id": 123,
                "userId": 456,
                "userNickname": "用户A",
                "userAvatar": "avatar_url",
                "content": "今天天气真好！",
                "images": ["url1", "url2"],
                "likeCount": 12,
                "commentCount": 5,
                "isLiked": true,
                "createTime": "2024-01-01 12:00:00",
                "comments": [
                    {
                        "id": 789,
                        "userId": 101,
                        "userNickname": "用户B",
                        "userAvatar": "avatar_url",
                        "content": "是的呢",
                        "createTime": "2024-01-01 12:05:00"
                    }
                ]
            }
        ],
        "total": 100,
        "current": 1,
        "size": 20
    }
}
```

### 6.3 前端组件设计

#### 6.3.1 核心组件
- `MomentPublisher`：动态发布组件
- `MomentCard`：动态卡片组件
- `CommentList`：评论列表组件
- `EmojiPicker`：表情选择器组件
- `ImageUploader`：图片上传组件

#### 6.3.2 状态管理
使用Pinia管理全局状态：
- `momentStore`：动态数据管理
- `userStore`：用户信息管理

## 7. 开发计划

### 7.1 开发阶段

#### Phase 1：基础功能（2周）
- 数据库表结构设计与创建
- 后端基础CRUD接口开发
- 前端基础页面框架搭建

#### Phase 2：核心功能（2周）
- 动态发布功能完整实现
- 动态列表展示与分页
- 点赞功能实现

#### Phase 3：完善功能（1周）
- 评论功能实现
- 图片上传优化
- 前端交互优化

#### Phase 4：测试优化（1周）
- 功能测试与bug修复
- 性能优化
- 用户体验优化

### 7.2 里程碑
- Week 2：后端接口开发完成
- Week 4：前端基础功能完成
- Week 5：完整功能测试通过
- Week 6：上线部署

## 8. 风险与应对

### 8.1 技术风险
**风险：** 图片上传性能问题  
**应对：** 使用CDN加速，图片压缩处理

**风险：** 并发点赞数据一致性  
**应对：** 使用Redis分布式锁，乐观锁机制

### 8.2 业务风险
**风险：** 用户发布不当内容  
**应对：** 敏感词过滤，举报机制，人工审核

**风险：** 用户活跃度不高  
**应对：** 增加激励机制，推送提醒

## 9. 上线计划

### 9.1 灰度发布
- 第一阶段：内部测试用户（50人）
- 第二阶段：部分活跃用户（500人）
- 第三阶段：全量发布

### 9.2 监控指标
- 系统性能：响应时间、错误率
- 业务指标：发布量、互动率、用户留存
- 用户反馈：满意度调研

## 10. 附录

### 10.1 相关文档
- UI设计稿
- 接口文档
- 数据库设计文档