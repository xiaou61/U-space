# 闪卡记忆系统 PRD v1.0.0

## 📋 项目概述

### 🎯 项目背景
面试备考过程中，开发者往往面临"学了就忘"的困境。传统刷题方式缺乏科学的复习机制，导致知识点无法长期记忆。闪卡记忆系统基于**间隔重复算法（Spaced Repetition）**，结合现有面试题库，帮助用户高效、科学地记忆知识点，实现"学习-练习-记忆"的完整闭环。

### 💡 核心价值
- **科学记忆**：基于艾宾浩斯遗忘曲线和SM-2算法，智能安排复习时间
- **高效学习**：专注薄弱知识点，避免重复学习已掌握内容
- **无缝整合**：与面试题库深度整合，一键生成闪卡
- **数据驱动**：详细的学习统计和记忆曲线可视化
- **碎片化学习**：支持随时随地快速复习，充分利用碎片时间

### 🔗 与现有模块关系
```
xiaou-interview (面试题库)
        │
        ▼ 一键生成
xiaou-flashcard (闪卡记忆系统)
        │
        ├─► xiaou-plan (计划打卡) - 每日复习任务
        ├─► xiaou-points (积分系统) - 复习奖励积分
        └─► xiaou-notification (消息通知) - 复习提醒
```

## 🚀 功能需求

### 1. 闪卡管理

#### 1.1 闪卡生成
- **从题库生成**：在面试题详情页，点击"生成闪卡"按钮
- **批量生成**：支持按题目分类/标签批量生成闪卡
- **自定义创建**：用户可手动创建个人闪卡
- **智能提取**：自动将题目标题作为正面，答案作为背面

#### 1.2 闪卡结构
```
┌─────────────────────────────────┐
│          【正面】               │
│                                 │
│    HashMap和Hashtable的区别？   │
│                                 │
│         [点击翻转]              │
└─────────────────────────────────┘
              ↓ 翻转
┌─────────────────────────────────┐
│          【背面】               │
│                                 │
│ 1. 线程安全：Hashtable线程安全  │
│ 2. null值：HashMap允许null      │
│ 3. 效率：HashMap效率更高        │
│ 4. 继承：HashMap继承AbstractMap │
│                                 │
│  [忘记] [模糊] [记得] [简单]    │
└─────────────────────────────────┘
```

#### 1.3 闪卡分组（卡组）
- **默认卡组**：按面试题分类自动创建（Java基础、Spring、MySQL等）
- **自定义卡组**：用户可创建个人卡组，自由组织闪卡
- **卡组统计**：展示卡组内闪卡数量、待复习数、掌握率

### 2. 间隔重复算法

#### 2.1 SM-2算法实现
基于SuperMemo SM-2算法，根据用户反馈动态调整复习间隔：

**用户反馈等级**：
| 等级 | 按钮 | 含义 | 间隔调整 |
|------|------|------|----------|
| 0 | 忘记 | 完全不记得 | 重置，10分钟后复习 |
| 1 | 模糊 | 记得一点 | 间隔缩短50% |
| 2 | 记得 | 稍加思考后想起 | 正常间隔 |
| 3 | 简单 | 轻松记得 | 间隔延长150% |

**复习间隔计算**：
```
初始间隔: 1天
第1次正确: 1天
第2次正确: 3天
第3次正确: 7天
第n次正确: 前次间隔 × 难度因子(EF)

难度因子(EF)范围: 1.3 ~ 2.5
EF调整公式: EF' = EF + (0.1 - (3-q) × (0.08 + (3-q) × 0.02))
其中q为用户评分(0-3)
```

#### 2.2 学习状态
```
┌─────────┐    首次学习    ┌─────────┐
│  新卡   │ ───────────► │  学习中  │
└─────────┘               └─────────┘
                               │
              ┌────────────────┼────────────────┐
              ▼                ▼                ▼
        ┌─────────┐      ┌─────────┐      ┌─────────┐
        │ 待复习  │◄────►│  复习中  │────►│  已掌握  │
        └─────────┘      └─────────┘      └─────────┘
              │                                 │
              └─────────── 遗忘 ────────────────┘
```

### 3. 学习模式

#### 3.1 每日学习
- **今日任务**：展示今日需复习的闪卡数量
- **新卡限制**：每日新学闪卡上限（默认20张，可调整）
- **复习优先**：优先复习到期闪卡，再学习新卡
- **学习目标**：可设置每日学习目标，与打卡系统联动

#### 3.2 自由学习
- **选择卡组**：选择特定卡组进行学习
- **随机模式**：随机抽取闪卡复习
- **顺序模式**：按添加顺序学习
- **仅复习**：只复习待复习的闪卡

#### 3.3 专项突破
- **薄弱攻克**：专门复习标记为"忘记"次数多的闪卡
- **收藏复习**：复习收藏的重点闪卡
- **限时挑战**：限时快速复习，测试反应速度

### 4. 用户端功能

#### 4.1 闪卡学习页面
```
+--------------------------------------------------+
|  🔙 返回  |  Java基础 (23/50)  |  ⚙️ 设置        |
+--------------------------------------------------+
|                                                  |
|  ┌──────────────────────────────────────────┐   |
|  │                                          │   |
|  │                                          │   |
|  │       HashMap和Hashtable的区别？         │   |
|  │                                          │   |
|  │                                          │   |
|  │              [显示答案]                  │   |
|  │                                          │   |
|  └──────────────────────────────────────────┘   |
|                                                  |
|  进度: ████████░░░░░░░░ 23/50                    |
|  今日已学: 23  |  待复习: 15  |  新学: 8         |
|                                                  |
+--------------------------------------------------+
```

#### 4.2 答案展示与反馈
```
+--------------------------------------------------+
|  🔙 返回  |  Java基础 (23/50)  |  ⚙️ 设置        |
+--------------------------------------------------+
|                                                  |
|  ┌──────────────────────────────────────────┐   |
|  │  HashMap和Hashtable的区别？              │   |
|  ├──────────────────────────────────────────┤   |
|  │                                          │   |
|  │  1. **线程安全**                         │   |
|  │     - Hashtable: 线程安全(synchronized)  │   |
|  │     - HashMap: 非线程安全                │   |
|  │                                          │   |
|  │  2. **null支持**                         │   |
|  │     - HashMap: 允许key/value为null       │   |
|  │     - Hashtable: 不允许null              │   |
|  │                                          │   |
|  │  3. **性能**：HashMap效率更高            │   |
|  │                                          │   |
|  └──────────────────────────────────────────┘   |
|                                                  |
|  ┌────────┬────────┬────────┬────────┐          |
|  │  忘记  │  模糊  │  记得  │  简单  │          |
|  │ <10min │  <1天  │  3天   │  7天   │          |
|  └────────┴────────┴────────┴────────┘          |
+--------------------------------------------------+
```

#### 4.3 学习统计面板
```
+--------------------------------------------------+
|  📊 学习统计                                     |
+--------------------------------------------------+
|                                                  |
|  今日学习                     本周趋势           |
|  ┌─────────────────┐         ┌─────────────┐    |
|  │ 已学习: 45张    │         │   ▄         │    |
|  │ 新学习: 12张    │         │  ▄█▄        │    |
|  │ 复习数: 33张    │         │ ▄███▄  ▄    │    |
|  │ 正确率: 78%     │         │▄█████▄▄█    │    |
|  └─────────────────┘         └─────────────┘    |
|                                                  |
|  记忆状态分布                                    |
|  ┌──────────────────────────────────────────┐   |
|  │ 已掌握 ████████████████████░░░░░ 68%     │   |
|  │ 学习中 ████████░░░░░░░░░░░░░░░░░ 22%     │   |
|  │ 待复习 ████░░░░░░░░░░░░░░░░░░░░░ 10%     │   |
|  └──────────────────────────────────────────┘   |
|                                                  |
|  连续学习: 🔥 7天  |  总学习: 156张  |  累计复习: 892次 |
+--------------------------------------------------+
```

### 5. 管理员端功能

#### 5.1 闪卡运营管理
- **官方卡组**：创建官方推荐卡组，设置推荐权重
- **内容审核**：审核用户公开分享的闪卡
- **数据统计**：查看整体闪卡使用情况、热门卡组

#### 5.2 学习数据分析
- **用户学习报告**：查看用户学习时长、复习频率
- **知识点分析**：统计哪些知识点遗忘率高
- **效果评估**：分析闪卡系统对面试通过率的影响

## 🗄️ 数据库设计

### 1. 闪卡表 (flashcard)
```sql
CREATE TABLE flashcard (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '闪卡ID',
    user_id BIGINT NOT NULL COMMENT '创建用户ID',
    deck_id BIGINT NOT NULL COMMENT '所属卡组ID',
    question_id BIGINT DEFAULT NULL COMMENT '关联的面试题ID',
    front_content TEXT NOT NULL COMMENT '正面内容(问题)',
    back_content LONGTEXT NOT NULL COMMENT '背面内容(答案)',
    source_type TINYINT DEFAULT 1 COMMENT '来源类型: 1-题库生成 2-用户创建',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_deck_id (deck_id),
    INDEX idx_question_id (question_id)
) COMMENT = '闪卡表';
```

### 2. 卡组表 (flashcard_deck)
```sql
CREATE TABLE flashcard_deck (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '卡组ID',
    user_id BIGINT NOT NULL COMMENT '所属用户ID (0为系统卡组)',
    name VARCHAR(100) NOT NULL COMMENT '卡组名称',
    description VARCHAR(500) DEFAULT NULL COMMENT '卡组描述',
    cover_image VARCHAR(500) DEFAULT NULL COMMENT '封面图片',
    category_id BIGINT DEFAULT NULL COMMENT '关联的面试题分类ID',
    card_count INT DEFAULT 0 COMMENT '闪卡总数',
    is_public TINYINT DEFAULT 0 COMMENT '是否公开: 0-私有 1-公开',
    is_official TINYINT DEFAULT 0 COMMENT '是否官方: 0-用户 1-官方',
    sort_order INT DEFAULT 0 COMMENT '排序权重',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_category_id (category_id),
    INDEX idx_is_public (is_public)
) COMMENT = '闪卡卡组表';
```

### 3. 学习记录表 (flashcard_study_record)
```sql
CREATE TABLE flashcard_study_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    card_id BIGINT NOT NULL COMMENT '闪卡ID',
    deck_id BIGINT NOT NULL COMMENT '卡组ID',
    ease_factor DECIMAL(4,2) DEFAULT 2.50 COMMENT '难度因子(EF)',
    interval_days INT DEFAULT 0 COMMENT '当前间隔天数',
    repetitions INT DEFAULT 0 COMMENT '连续正确次数',
    quality INT DEFAULT NULL COMMENT '最近一次评分(0-3)',
    next_review_date DATE NOT NULL COMMENT '下次复习日期',
    last_review_time DATETIME DEFAULT NULL COMMENT '最近复习时间',
    total_reviews INT DEFAULT 0 COMMENT '总复习次数',
    correct_count INT DEFAULT 0 COMMENT '正确次数',
    wrong_count INT DEFAULT 0 COMMENT '错误次数',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-新卡 1-学习中 2-已掌握',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_card (user_id, card_id),
    INDEX idx_user_id (user_id),
    INDEX idx_next_review (user_id, next_review_date),
    INDEX idx_deck_id (deck_id)
) COMMENT = '闪卡学习记录表';
```

### 4. 每日学习统计表 (flashcard_daily_stats)
```sql
CREATE TABLE flashcard_daily_stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    study_date DATE NOT NULL COMMENT '学习日期',
    new_count INT DEFAULT 0 COMMENT '新学数量',
    review_count INT DEFAULT 0 COMMENT '复习数量',
    correct_count INT DEFAULT 0 COMMENT '正确数量',
    wrong_count INT DEFAULT 0 COMMENT '错误数量',
    study_time_seconds INT DEFAULT 0 COMMENT '学习时长(秒)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_date (user_id, study_date),
    INDEX idx_user_id (user_id),
    INDEX idx_study_date (study_date)
) COMMENT = '闪卡每日学习统计表';
```

### 5. 学习详情记录表 (flashcard_review_log)
```sql
CREATE TABLE flashcard_review_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    card_id BIGINT NOT NULL COMMENT '闪卡ID',
    quality INT NOT NULL COMMENT '评分(0-3)',
    ease_factor_before DECIMAL(4,2) COMMENT '复习前EF',
    ease_factor_after DECIMAL(4,2) COMMENT '复习后EF',
    interval_before INT COMMENT '复习前间隔',
    interval_after INT COMMENT '复习后间隔',
    review_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '复习时间',
    time_spent_ms INT DEFAULT 0 COMMENT '思考时长(毫秒)',
    INDEX idx_user_id (user_id),
    INDEX idx_card_id (card_id),
    INDEX idx_review_time (review_time)
) COMMENT = '闪卡复习详情日志表';
```

## 🔌 API接口设计

### 1. 卡组管理

```http
# 获取用户卡组列表
GET /api/flashcard/decks
Response: {
  "code": 200,
  "data": {
    "official": [...],    // 官方卡组
    "personal": [...],    // 个人卡组
    "subscribed": [...]   // 订阅的公开卡组
  }
}

# 创建卡组
POST /api/flashcard/decks
{
  "name": "Spring核心知识点",
  "description": "Spring框架面试必备",
  "isPublic": false
}

# 更新卡组
PUT /api/flashcard/decks/{deckId}

# 删除卡组
DELETE /api/flashcard/decks/{deckId}
```

### 2. 闪卡管理

```http
# 从面试题生成闪卡
POST /api/flashcard/generate
{
  "questionId": 12345,
  "deckId": 1
}

# 批量从分类生成闪卡
POST /api/flashcard/generate/batch
{
  "categoryId": 10,
  "deckId": 1
}

# 创建自定义闪卡
POST /api/flashcard/cards
{
  "deckId": 1,
  "frontContent": "什么是Spring IOC?",
  "backContent": "IOC（控制反转）是..."
}

# 获取卡组内的闪卡列表
GET /api/flashcard/decks/{deckId}/cards?page=1&size=20

# 更新闪卡
PUT /api/flashcard/cards/{cardId}

# 删除闪卡
DELETE /api/flashcard/cards/{cardId}
```

### 3. 学习相关

```http
# 获取今日学习任务
GET /api/flashcard/study/today
Response: {
  "code": 200,
  "data": {
    "newCount": 20,       // 新卡数量
    "reviewCount": 35,    // 待复习数量
    "totalToday": 55,     // 今日总任务
    "completedToday": 12  // 今日已完成
  }
}

# 开始学习（获取下一张闪卡）
POST /api/flashcard/study/start
{
  "deckId": 1,            // 可选，指定卡组
  "mode": "daily"         // daily-每日任务 / free-自由学习 / weak-薄弱攻克
}
Response: {
  "code": 200,
  "data": {
    "cardId": 123,
    "frontContent": "HashMap和Hashtable的区别？",
    "backContent": "1. 线程安全...",
    "isNew": true,
    "reviewCount": 0,
    "progress": {
      "current": 1,
      "total": 55
    }
  }
}

# 提交学习反馈
POST /api/flashcard/study/review
{
  "cardId": 123,
  "quality": 2,           // 0-忘记 1-模糊 2-记得 3-简单
  "timeSpentMs": 5000     // 思考时长
}
Response: {
  "code": 200,
  "data": {
    "nextReviewDate": "2025-12-17",
    "intervalDays": 7,
    "hasNext": true
  }
}

# 获取下一张闪卡
GET /api/flashcard/study/next
```

### 4. 统计相关

```http
# 获取学习统计概览
GET /api/flashcard/stats/overview
Response: {
  "code": 200,
  "data": {
    "totalCards": 156,
    "masteredCards": 98,
    "learningCards": 35,
    "newCards": 23,
    "streakDays": 7,
    "totalReviews": 892
  }
}

# 获取每日学习趋势
GET /api/flashcard/stats/daily?days=7
Response: {
  "code": 200,
  "data": [
    {"date": "2025-12-04", "newCount": 15, "reviewCount": 30, "correctRate": 0.85},
    {"date": "2025-12-05", "newCount": 12, "reviewCount": 28, "correctRate": 0.78},
    ...
  ]
}

# 获取卡组学习进度
GET /api/flashcard/stats/decks
Response: {
  "code": 200,
  "data": [
    {"deckId": 1, "name": "Java基础", "total": 50, "mastered": 35, "progress": 0.70},
    ...
  ]
}

# 获取记忆热力图
GET /api/flashcard/stats/heatmap?year=2025
```

### 5. 管理员接口

```http
# 获取官方卡组列表
GET /api/admin/flashcard/decks

# 创建官方卡组
POST /api/admin/flashcard/decks
{
  "name": "Java面试宝典",
  "categoryId": 1,
  "isOfficial": true,
  "sortOrder": 100
}

# 从题库分类批量生成官方闪卡
POST /api/admin/flashcard/generate/category
{
  "categoryId": 1,
  "deckId": 1
}

# 获取用户学习数据统计
GET /api/admin/flashcard/stats/users

# 获取知识点遗忘率统计
GET /api/admin/flashcard/stats/forget-rate
```

## ⚙️ 技术实现方案

### 1. 模块结构 (xiaou-flashcard)

```
xiaou-flashcard/
├── controller/
│   ├── FlashcardDeckController.java      # 卡组管理
│   ├── FlashcardController.java          # 闪卡管理
│   ├── FlashcardStudyController.java     # 学习相关
│   ├── FlashcardStatsController.java     # 统计相关
│   └── admin/
│       └── AdminFlashcardController.java # 管理员接口
├── domain/
│   ├── Flashcard.java                    # 闪卡实体
│   ├── FlashcardDeck.java                # 卡组实体
│   ├── FlashcardStudyRecord.java         # 学习记录实体
│   ├── FlashcardDailyStats.java          # 每日统计实体
│   └── FlashcardReviewLog.java           # 复习日志实体
├── dto/
│   ├── request/
│   │   ├── CreateDeckRequest.java
│   │   ├── CreateCardRequest.java
│   │   ├── GenerateCardRequest.java
│   │   ├── StudyStartRequest.java
│   │   └── ReviewSubmitRequest.java
│   └── response/
│       ├── DeckListResponse.java
│       ├── StudyCardResponse.java
│       ├── StatsOverviewResponse.java
│       └── ReviewResultResponse.java
├── service/
│   ├── FlashcardDeckService.java
│   ├── FlashcardService.java
│   ├── FlashcardStudyService.java
│   ├── FlashcardStatsService.java
│   ├── SpacedRepetitionService.java      # SM-2算法服务
│   └── impl/
│       ├── FlashcardDeckServiceImpl.java
│       ├── FlashcardServiceImpl.java
│       ├── FlashcardStudyServiceImpl.java
│       ├── FlashcardStatsServiceImpl.java
│       └── SpacedRepetitionServiceImpl.java
├── mapper/
│   ├── FlashcardMapper.java
│   ├── FlashcardDeckMapper.java
│   ├── FlashcardStudyRecordMapper.java
│   ├── FlashcardDailyStatsMapper.java
│   └── FlashcardReviewLogMapper.java
└── algorithm/
    └── SM2Algorithm.java                  # SM-2算法实现
```

### 2. SM-2算法核心实现

```java
/**
 * SM-2间隔重复算法实现
 */
public class SM2Algorithm {
    
    private static final double DEFAULT_EF = 2.5;
    private static final double MIN_EF = 1.3;
    private static final double MAX_EF = 2.5;
    
    /**
     * 计算下次复习间隔
     * @param quality 用户评分 0-3
     * @param repetitions 连续正确次数
     * @param easeFactor 当前难度因子
     * @param lastInterval 上次间隔天数
     * @return 计算结果
     */
    public static SM2Result calculate(int quality, int repetitions, 
                                       double easeFactor, int lastInterval) {
        SM2Result result = new SM2Result();
        
        // 计算新的难度因子
        double newEF = easeFactor + (0.1 - (3 - quality) * (0.08 + (3 - quality) * 0.02));
        newEF = Math.max(MIN_EF, Math.min(MAX_EF, newEF));
        result.setEaseFactor(newEF);
        
        // 根据评分计算间隔
        if (quality < 1) {
            // 忘记：重置
            result.setRepetitions(0);
            result.setIntervalDays(0); // 10分钟后（用分钟表示）
            result.setNextReviewMinutes(10);
        } else {
            // 记住：计算新间隔
            int newRepetitions = repetitions + 1;
            result.setRepetitions(newRepetitions);
            
            int interval;
            if (newRepetitions == 1) {
                interval = 1;
            } else if (newRepetitions == 2) {
                interval = 3;
            } else {
                interval = (int) Math.round(lastInterval * newEF);
            }
            
            // 根据评分微调
            if (quality == 1) {
                interval = (int) Math.max(1, interval * 0.5);
            } else if (quality == 3) {
                interval = (int) (interval * 1.5);
            }
            
            result.setIntervalDays(interval);
        }
        
        return result;
    }
}
```

### 3. 前端组件设计

```
vue3-user-front/src/views/flashcard/
├── index.vue                    # 闪卡首页（卡组列表）
├── DeckDetail.vue               # 卡组详情
├── Study.vue                    # 学习页面
├── Stats.vue                    # 统计页面
├── components/
│   ├── FlashcardItem.vue        # 闪卡卡片组件
│   ├── FlipCard.vue             # 翻转卡片动画组件
│   ├── ReviewButtons.vue        # 评分按钮组件
│   ├── ProgressBar.vue          # 学习进度条
│   ├── StudyStats.vue           # 学习统计面板
│   ├── HeatmapChart.vue         # 热力图组件
│   └── DeckCard.vue             # 卡组卡片组件
└── api/
    └── flashcard.js             # API接口封装
```

### 4. 与现有模块集成

#### 4.1 与面试题库集成 (xiaou-interview)
```java
// 在面试题详情接口中添加闪卡状态
public class QuestionDetailResponse {
    // ... 原有字段
    private Boolean hasFlashcard;      // 是否已生成闪卡
    private Long flashcardId;          // 闪卡ID
}

// 一键生成闪卡接口
@PostMapping("/flashcard/generate")
public Result<Long> generateFromQuestion(@RequestBody GenerateCardRequest request) {
    // 1. 查询面试题详情
    // 2. 提取标题和答案
    // 3. 创建闪卡
    // 4. 返回闪卡ID
}
```

#### 4.2 与计划打卡集成 (xiaou-plan)
```java
// 每日闪卡学习任务作为打卡项
public class DailyPlanTask {
    private String taskType = "flashcard_study";
    private Integer targetCount;    // 目标学习数量
    private Integer completedCount; // 已完成数量
}
```

#### 4.3 与积分系统集成 (xiaou-points)
```java
// 闪卡学习积分规则
public enum FlashcardPointsRule {
    DAILY_STUDY_COMPLETE(10, "完成每日学习任务"),
    STREAK_7_DAYS(50, "连续学习7天"),
    STREAK_30_DAYS(200, "连续学习30天"),
    MASTER_DECK(30, "掌握一个卡组");
}
```

## 📋 开发计划

### Phase 1: 基础架构 (1周)
- [ ] 创建 xiaou-flashcard 模块
- [ ] 数据库表结构设计与创建
- [ ] 基础实体类和Mapper
- [ ] SM-2算法核心实现

### Phase 2: 核心功能 (2周)
- [ ] 卡组CRUD接口
- [ ] 闪卡CRUD接口
- [ ] 从面试题生成闪卡功能
- [ ] 学习流程核心接口（开始、下一张、提交反馈）
- [ ] 间隔重复算法集成

### Phase 3: 用户端前端 (2周)
- [ ] 卡组列表页面
- [ ] 闪卡学习页面（翻转动画）
- [ ] 评分反馈组件
- [ ] 学习进度展示
- [ ] 统计面板页面

### Phase 4: 管理员端 (1周)
- [ ] 官方卡组管理
- [ ] 批量生成闪卡
- [ ] 数据统计看板

### Phase 5: 集成与优化 (1周)
- [ ] 与打卡系统集成
- [ ] 与积分系统集成
- [ ] 复习提醒通知
- [ ] 性能优化与测试

## 🎯 成功指标

### 功能指标
- 闪卡翻转动画流畅，延迟 < 100ms
- 学习页面加载时间 < 500ms
- SM-2算法计算响应时间 < 50ms
- 支持单用户 10000+ 闪卡

### 用户体验指标
- 每日学习完成率 > 70%
- 7天留存率 > 50%
- 用户平均每次学习时长 > 10分钟

### 业务指标
- 用户知识点记忆正确率提升 > 30%
- 与题库联动使用率 > 60%

## 🔮 未来规划

### v1.1.0
- **语音播放**：支持闪卡内容语音朗读
- **图片闪卡**：支持图片类型的闪卡
- **分享功能**：支持分享卡组给其他用户

### v1.2.0
- **AI生成**：基于Coze自动生成闪卡内容优化
- **智能推荐**：根据学习数据推荐相关闪卡
- **协作编辑**：支持多人协作维护卡组

### v2.0.0
- **移动端App**：独立的闪卡学习App
- **离线模式**：支持离线学习和同步
- **游戏化**：添加成就系统、排行榜等激励机制

---

*文档版本: v1.0.0*  
*创建时间: 2025-12-10*  
*最后更新: 2025-12-10*
