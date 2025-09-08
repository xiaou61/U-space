# 消息通知模块 PRD v1.0.0

## 1. 产品概述

### 1.1 产品定位
消息通知模块是一个通用的站内消息推送系统，为全站提供统一的消息通知服务，支持多种消息类型和灵活的扩展机制。

### 1.2 核心价值
- **统一性**：为全站所有模块提供统一的消息通知入口
- **通用性**：支持多种消息类型，易于集成和扩展
- **实时性**：支持实时消息推送和状态更新
- **可管理性**：提供完整的消息管理和统计功能

## 2. 功能需求

### 2.1 核心功能

#### 2.1.1 消息发送
- **系统公告**：管理员发布全站公告，自动推送给所有用户
- **个人消息**：支持点对点消息推送
- **批量消息**：支持批量用户消息推送
- **模块集成**：其他模块通过统一接口发送消息

#### 2.1.2 消息接收
- **消息列表**：用户查看消息列表，支持分页和筛选
- **消息详情**：查看消息详细内容
- **消息状态**：标记已读/未读、删除消息
- **消息统计**：显示未读消息数量

#### 2.1.3 消息管理
- **消息模板**：预定义消息模板，支持参数替换
- **消息分类**：按类型、来源模块分类管理
- **消息审核**：重要消息支持审核机制
- **消息统计**：发送统计、阅读统计等

### 2.2 消息类型设计

| 消息类型 | 代码 | 描述 | 示例 |
|---------|------|------|------|
| 系统公告 | SYSTEM_ANNOUNCEMENT | 管理员发布的全站公告 | 系统维护通知 |
| 社区互动 | COMMUNITY_INTERACTION | 社区模块相关通知 | 帖子被点赞、评论 |
| 面试提醒 | INTERVIEW_REMINDER | 面试模块相关通知 | 收藏的题目更新 |
| 系统通知 | SYSTEM_NOTIFICATION | 系统自动通知 | 账号异常登录 |
| 审核结果 | AUDIT_RESULT | 内容审核结果通知 | 帖子审核通过/拒绝 |
| 活动通知 | ACTIVITY_NOTIFICATION | 活动相关通知 | 活动开始提醒 |

### 2.3 消息优先级

| 优先级 | 代码 | 描述 | 展示方式 |
|--------|------|------|----------|
| 高 | HIGH | 重要紧急消息 | 红色标识，强制弹窗 |
| 中 | MEDIUM | 一般重要消息 | 橙色标识，普通通知 |
| 低 | LOW | 普通消息 | 默认样式 |

## 3. 技术设计

### 3.1 模块架构
```
xiaou-notification/
├── controller/
│   ├── admin/           # 管理端接口
│   │   ├── AdminNotificationController.java
│   │   └── AdminAnnouncementController.java
│   └── pub/             # 用户端接口
│       └── NotificationController.java
├── domain/              # 实体类
│   ├── Notification.java
│   ├── NotificationTemplate.java
│   └── NotificationConfig.java
├── dto/                 # 数据传输对象
├── mapper/              # MyBatis Mapper
├── service/             # 业务服务
│   ├── NotificationService.java
│   ├── NotificationSender.java
│   └── NotificationTemplate.java
└── utils/               # 工具类
    └── NotificationUtil.java
```

### 3.2 数据库设计

#### 3.2.1 消息主表（notification）
```sql
CREATE TABLE notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    title VARCHAR(200) NOT NULL COMMENT '消息标题',
    content TEXT COMMENT '消息内容',
    type VARCHAR(50) NOT NULL COMMENT '消息类型',
    priority VARCHAR(20) DEFAULT 'LOW' COMMENT '优先级',
    sender_id BIGINT COMMENT '发送者ID',
    receiver_id BIGINT COMMENT '接收者ID（为空表示全站公告）',
    source_module VARCHAR(50) COMMENT '来源模块',
    source_id VARCHAR(100) COMMENT '来源数据ID',
    status VARCHAR(20) DEFAULT 'UNREAD' COMMENT '状态：UNREAD/READ/DELETED',
    read_time DATETIME COMMENT '阅读时间',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_receiver_status (receiver_id, status),
    INDEX idx_type_created (type, created_time),
    INDEX idx_source (source_module, source_id)
) COMMENT='消息通知表';
```

#### 3.2.2 消息模板表（notification_template）
```sql
CREATE TABLE notification_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模板ID',
    name VARCHAR(100) NOT NULL COMMENT '模板名称',
    type VARCHAR(50) NOT NULL COMMENT '消息类型',
    title_template VARCHAR(200) NOT NULL COMMENT '标题模板',
    content_template TEXT NOT NULL COMMENT '内容模板',
    params JSON COMMENT '参数配置',
    is_enabled TINYINT DEFAULT 1 COMMENT '是否启用',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_name (name)
) COMMENT='消息模板表';
```

#### 3.2.3 消息配置表（notification_config）
```sql
CREATE TABLE notification_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    type VARCHAR(50) NOT NULL COMMENT '消息类型',
    is_enabled TINYINT DEFAULT 1 COMMENT '是否启用接收',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_type (user_id, type)
) COMMENT='消息配置表';
```

### 3.3 核心接口设计

#### 3.3.1 消息发送接口
```java
/**
 * 发送消息统一接口
 */
public interface NotificationSender {
    
    /**
     * 发送单个消息
     */
    boolean sendNotification(SendNotificationRequest request);
    
    /**
     * 发送批量消息
     */
    boolean sendBatchNotification(BatchSendRequest request);
    
    /**
     * 发送模板消息
     */
    boolean sendTemplateNotification(TemplateNotificationRequest request);
    
    /**
     * 发送全站公告
     */
    boolean sendAnnouncement(AnnouncementRequest request);
}
```

## 4. 页面设计

### 4.1 用户端页面

#### 4.1.1 消息中心
- **路径**：`/message/center`
- **功能**：
  - 消息列表展示（分页）
  - 消息类型筛选
  - 批量操作（标记已读、删除）
  - 消息搜索
  - 未读消息统计

#### 4.1.2 消息详情
- **路径**：`/message/detail/:id`
- **功能**：
  - 消息详细内容展示
  - 相关操作（删除、标记已读）
  - 相关链接跳转

#### 4.1.3 消息设置
- **路径**：`/message/settings`
- **功能**：
  - 各类型消息接收开关
  - 消息提醒方式设置

### 4.2 管理端页面

#### 4.2.1 消息管理
- **路径**：`/admin/notification/messages`
- **功能**：
  - 消息列表查看
  - 消息统计报表
  - 消息详情查看

#### 4.2.2 公告管理
- **路径**：`/admin/notification/announcements`
- **功能**：
  - 发布系统公告
  - 公告草稿管理
  - 公告发送记录

#### 4.2.3 消息模板
- **路径**：`/admin/notification/templates`
- **功能**：
  - 模板列表管理
  - 模板编辑器（支持参数占位符）
  - 模板预览测试

#### 4.2.4 消息统计
- **路径**：`/admin/notification/statistics`
- **功能**：
  - 发送量统计
  - 阅读率统计
  - 用户行为分析

## 5. 集成方案

- 暂时不与其他模块进行基础

## 6. 技术要点

### 6.1 性能优化
- **异步处理**：消息发送采用异步处理，避免阻塞主流程
- **批量处理**：支持批量发送和批量更新状态
- **缓存策略**：未读消息数量使用Redis缓存

### 6.2 扩展性设计
- **消息类型扩展**：通过枚举和配置支持新消息类型
- **推送方式扩展**：预留邮件、短信等推送方式接口
- **模板系统**：支持复杂的消息模板和参数替换

### 6.3 可靠性保证
- **消息去重**：防止重复发送相同消息
- **失败重试**：发送失败时支持重试机制
- **数据一致性**：保证消息状态的一致性

