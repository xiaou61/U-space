# 面试题模块PRD

## 1. 产品概述

### 1.1 产品目标
提供一个面试题管理平台，分为官方面试题跟用户自主上传的面试题。通过题单+分类的方式组织内容，支持MD格式批量导入和收藏功能。

### 1.2 用户群体
- **管理员**：管理官方题单和分类，审核用户内容
- **普通用户**：创建私人题单，浏览学习面试题，收藏感兴趣的内容

## 2. 功能需求

### 2.1 核心概念
- **题单**：一组相关题目的集合，分为官方题单和用户私人题单
- **分类**：对题单的分类管理（如Java、前端、算法、数据库等）
- **题目**：属于某个题单的具体简答题
- **收藏**：用户可以收藏题单或单个题目

### 2.2 管理端功能

#### 2.2.1 分类管理
- **分类列表**：管理题单分类
- **新增分类**：创建新的分类
- **编辑分类**：修改分类信息
- **删除分类**：删除分类（需检查是否有关联题单）

#### 2.2.2 官方题单管理
- **官方题单列表**：管理所有官方题单
- **新增官方题单**：创建官方题单
- **编辑官方题单**：修改官方题单信息
- **题目管理**：管理题单内的题目

#### 2.2.3 用户内容审核
- **用户题单审核**：审核用户公开的题单
- **内容管理**：管理不当内容

### 2.3 用户端功能

#### 2.3.1 题单浏览
- **题单列表**：分页浏览所有公开题单（官方+用户公开）
- **分类浏览**：按分类查看题单
- **题单详情**：查看题单信息和包含的题目
- **搜索功能**：根据关键词搜索题单和题目

#### 2.3.2 个人题单管理
- **我的题单**：管理个人创建的题单
- **创建题单**：创建新的私人题单
- **编辑题单**：修改题单信息和题目
- **公开设置**：设置题单为公开或私有
- **题目管理**：
  - MD批量导入：通过MD格式文本批量导入题目（唯一的题目添加方式）

#### 2.3.3 学习功能
- **题目学习**：浏览题目和答案 我们分为俩个模式，一个叫做背题模式，一个叫做答题模式，答题模式目前做的就是，我们会隐藏答案，然后用户点击中间的按钮查看题目后，就可以查看题目对应的答案了。
- **收藏功能**：收藏题单或题目
- **学习记录**：记录浏览历史

## 3. 数据模型设计

### 3.1 题单表 (interview_question_set)
```sql
id                 BIGINT         题单ID (主键)
title              VARCHAR(200)   题单标题
description        TEXT           题单描述
category_id        BIGINT         分类ID
type               TINYINT        类型 (1-官方 2-用户创建)
visibility         TINYINT        可见性 (1-公开 2-私有) 仅用户创建题单有效
question_count     INT            题目数量
view_count         INT            浏览次数
favorite_count     INT            收藏次数
status             TINYINT        状态 (0-草稿 1-发布 2-下线)
creator_id         BIGINT         创建人ID
creator_name       VARCHAR(50)    创建人姓名
create_time        DATETIME       创建时间
update_time        DATETIME       更新时间
```

### 3.2 题目表 (interview_question)
```sql
id                 BIGINT         题目ID (主键)
question_set_id    BIGINT         所属题单ID
title              VARCHAR(500)   题目标题
content            TEXT           题目内容 (Markdown格式)
answer             TEXT           参考答案 (Markdown格式)
sort_order         INT            题单内排序
view_count         INT            浏览次数
favorite_count     INT            收藏次数
create_time        DATETIME       创建时间
update_time        DATETIME       更新时间
```

### 3.3 分类表 (interview_category)
```sql
id                 BIGINT         分类ID (主键)
name               VARCHAR(100)   分类名称
description        VARCHAR(500)   分类描述
sort_order         INT            排序序号
question_set_count INT            题单数量
status             TINYINT        状态 (0-禁用 1-启用)
create_time        DATETIME       创建时间
update_time        DATETIME       更新时间
```

### 3.4 收藏表 (interview_favorite)
```sql
id                 BIGINT         收藏ID (主键)
user_id            BIGINT         用户ID
target_type        TINYINT        收藏类型 (1-题单 2-题目)
target_id          BIGINT         目标ID（题单ID或题目ID）
create_time        DATETIME       收藏时间
```

## 4. MD格式规范

### 4.1 MD格式示例
```markdown
## 什么是Java中的多态？

多态是面向对象编程的一个重要特性，指同一个接口可以有不同的实现方式。

在Java中，多态主要体现在：
- 方法重载（编译时多态）
- 方法重写（运行时多态）

**参考答案：**
多态是指同一个方法在不同对象上有不同的实现。在Java中，多态主要通过继承和接口实现。

运行时多态的实现条件：
1. 继承关系
2. 方法重写
3. 父类引用指向子类对象

## Spring Bean的生命周期是什么？

请详细描述Spring Bean从创建到销毁的整个生命周期过程。

**参考答案：**
Spring Bean的生命周期包括以下几个阶段：

1. **实例化**：创建Bean对象
2. **属性注入**：设置Bean的属性值
3. **初始化**：
   - BeanNameAware.setBeanName()
   - BeanFactoryAware.setBeanFactory()
   - ApplicationContextAware.setApplicationContext()
   - BeanPostProcessor.postProcessBeforeInitialization()
   - InitializingBean.afterPropertiesSet()
   - 自定义init方法
   - BeanPostProcessor.postProcessAfterInitialization()
4. **使用**：Bean可以被应用程序使用
5. **销毁**：
   - DisposableBean.destroy()
   - 自定义destroy方法
```

### 4.2 解析规则
- 使用 `## ` 作为题目分割标识
- `## ` 后的内容作为题目标题
- 题目标题下到`**参考答案：**`之前的内容作为题目内容（MD格式）
- `**参考答案：**` 后的内容作为参考答案（MD格式）
- 如果没有参考答案标识，则`## `后到下一个`## `之前的所有内容作为题目内容

### 4.3 MD渲染支持
- **前端渲染**：使用markdown-it或marked.js将MD内容渲染为HTML
- **代码高亮**：支持代码块语法高亮
- **列表支持**：支持有序列表、无序列表
- **格式支持**：支持粗体、斜体、行内代码等基础格式

## 5. API接口设计

### 5.1 管理端接口

#### 5.1.1 分类管理
- `POST /interview/admin/categories` - 创建分类
- `GET /interview/admin/categories` - 获取分类列表
- `PUT /interview/admin/categories/{id}` - 更新分类
- `DELETE /interview/admin/categories/{id}` - 删除分类

#### 5.1.2 官方题单管理
- `POST /interview/admin/question-sets` - 创建官方题单
- `GET /interview/admin/question-sets` - 分页查询官方题单
- `PUT /interview/admin/question-sets/{id}` - 更新官方题单
- `DELETE /interview/admin/question-sets/{id}` - 删除官方题单

### 5.2 用户端接口

#### 5.2.1 题单浏览
- `GET /interview/question-sets` - 分页查询公开题单
- `GET /interview/question-sets/{id}` - 获取题单详情
- `GET /interview/question-sets/{id}/questions` - 获取题单内的题目列表
- `GET /interview/questions/{id}` - 获取题目详情

#### 5.2.2 个人题单管理
- `POST /interview/my/question-sets` - 创建个人题单
- `GET /interview/my/question-sets` - 获取我的题单列表
- `PUT /interview/my/question-sets/{id}` - 更新个人题单
- `DELETE /interview/my/question-sets/{id}` - 删除个人题单
- `POST /interview/my/question-sets/{id}/import-md` - MD批量导入（唯一的题目添加方式）

#### 5.2.3 分类和搜索
- `GET /interview/categories` - 获取启用的分类列表
- `GET /interview/search` - 搜索题单和题目

#### 5.2.4 收藏功能
- `POST /interview/favorites` - 添加收藏
- `DELETE /interview/favorites` - 取消收藏
- `GET /interview/favorites/question-sets` - 获取收藏的题单
- `GET /interview/favorites/questions` - 获取收藏的题目

## 6. 页面设计

### 6.1 管理端页面

#### 6.1.1 分类管理页面
- **分类列表页** (`/admin/interview/categories`)
  - 分类列表：显示分类信息和题单数量
  - 操作按钮：新增、编辑、删除、排序

#### 6.1.2 官方题单管理
- **题单列表页** (`/admin/interview/question-sets`)
  - 题单列表：显示题单基本信息
  - 操作按钮：新增、编辑、删除、查看题目

### 6.2 用户端页面

#### 6.2.1 题单浏览页面
- **题单广场** (`/interview`)
  - 分类导航：快速切换分类
  - 搜索功能：关键词搜索
  - 题单卡片：显示题单信息（标题、描述、题目数量、创建者）
  - 分页组件：分页浏览

- **题单详情页** (`/interview/question-sets/:id`)
  - 题单信息：标题、描述、分类、创建者
  - 操作按钮：收藏题单、学习模式切换
  - 题目列表：显示题单内所有题目
  - 题目操作：查看详情、收藏题目

- **题目详情页** (`/interview/questions/:id`)
  - **背题模式**：题目和答案都显示，支持MD渲染
  - **答题模式**：初始只显示题目，点击"查看答案"按钮显示答案
  - 操作按钮：收藏题目、切换学习模式
  - 导航：上一题/下一题、返回题单

#### 6.2.2 个人中心页面
- **我的题单** (`/interview/my/question-sets`)
  - 题单列表：显示个人创建的题单
  - 操作按钮：新增、编辑、删除、公开设置

- **创建/编辑题单** (`/interview/my/question-sets/add`, `/interview/my/question-sets/edit/:id`)
  - 基本信息：标题、描述、分类、公开设置
  - MD导入区域：
    - 大型文本框（支持MD语法高亮）
    - MD格式说明和示例
    - 实时预览功能
    - 解析结果预览（显示将生成多少题目）
  - 题目列表：导入后的题目列表，支持预览、删除、重新排序

- **我的收藏** (`/interview/favorites`)
  - 收藏的题单：分页显示
  - 收藏的题目：分页显示

## 7. 技术实现要点

### 7.1 MD处理功能
- **前端**：
  - MD编辑器：CodeMirror或Monaco Editor，支持MD语法高亮
  - MD渲染：markdown-it渲染MD内容为HTML
  - 代码高亮：highlight.js支持代码块高亮
  - 实时预览：编辑时实时显示渲染效果

- **后端**：
  - 接收MD格式文本
  - 按 `## ` 分割题目
  - 正则表达式解析题目标题、内容、答案
  - 批量插入题目表（content和answer字段存储原始MD文本）

### 7.2 权限控制
- **官方题单**：仅管理员可创建和编辑
- **用户题单**：用户只能操作自己创建的题单
- **公开题单**：所有用户可浏览
- **私有题单**：仅创建者可见

### 7.3 性能优化
- **分页查询**：题单和题目列表使用分页
- **缓存策略**：热门题单使用Redis缓存(一般情况下我们会对某个题单里面的对应关系进行缓存，题目的话，如果用户看过的话我们直接缓存到本地缓存中就可以了)
- **搜索优化**：基于数据库索引的模糊查询(这个可以查询除了题目外，具体的内容也要支持查询)
- **MD渲染优化**：客户端缓存渲染结果，避免重复渲染

