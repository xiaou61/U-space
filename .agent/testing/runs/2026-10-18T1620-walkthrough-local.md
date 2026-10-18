---
artifact: test-run
run_id: 2026-10-18T1620
layer: walkthrough
target: local
status: passed
started: 2026-10-18 16:20:15
---

# 全接口自查 2026-10-18 16:20:15

合计 **69** 步：通过 69，失败 0；覆盖端点 **62** 条。

| # | 检查项 | 方法 | 路径 | 状态码 | 结果 |
| --- | --- | --- | --- | --- | --- |
| 1 | 图形验证码 | POST | `/api/v1/auth/captcha` | 200 | 通过 |
| 2 | 图形验证码返回 id | - | `-` | 0 | 通过 |
| 3 | 发邮箱验证码 | POST | `/api/v1/auth/email-code` | 200 | 通过 |
| 4 | 注册 | POST | `/api/v1/auth/register` | 200 | 通过 |
| 5 | 登录 | POST | `/api/v1/auth/login` | 200 | 通过 |
| 6 | 刷新令牌 | POST | `/api/v1/auth/refresh` | 200 | 通过 |
| 7 | 当前身份 | GET | `/api/v1/me` | 200 | 通过 |
| 8 | 读画像 | GET | `/api/v1/account/profile` | 200 | 通过 |
| 9 | 写画像 | PUT | `/api/v1/account/profile` | 200 | 通过 |
| 10 | 画像可读可写 | - | `-` | 0 | 通过 |
| 11 | 建分类 | POST | `/api/v1/knowledge/admin/categories` | 200 | 通过 |
| 12 | 管理端分类列表 | GET | `/api/v1/knowledge/admin/categories` | 200 | 通过 |
| 13 | 改分类 | PUT | `/api/v1/knowledge/admin/categories/42` | 200 | 通过 |
| 14 | 建条目 | POST | `/api/v1/knowledge/admin/entries` | 200 | 通过 |
| 15 | 再建一条（供关系用） | POST | `/api/v1/knowledge/admin/entries` | 200 | 通过 |
| 16 | 管理端条目列表 | GET | `/api/v1/knowledge/admin/entries?size=5` | 200 | 通过 |
| 17 | 管理端条目详情 | GET | `/api/v1/knowledge/admin/entries/78` | 200 | 通过 |
| 18 | 改条目 | PUT | `/api/v1/knowledge/admin/entries/78` | 200 | 通过 |
| 19 | 建关系 | POST | `/api/v1/knowledge/admin/relations` | 200 | 通过 |
| 20 | 删关系 | DELETE | `/api/v1/knowledge/admin/relations/29` | 200 | 通过 |
| 21 | 上传附件 | POST | `/api/v1/knowledge/admin/files` | 200 | 通过 |
| 22 | 分类树 | GET | `/api/v1/knowledge/categories` | 200 | 通过 |
| 23 | 条目列表 | GET | `/api/v1/knowledge/entries?size=5` | 200 | 通过 |
| 24 | 条目详情 | GET | `/api/v1/knowledge/entries/78` | 200 | 通过 |
| 25 | 附件读取（免鉴权） | GET | `/api/v1/knowledge/files/8ba551f9-9406-4fdf-8ff9-f16c2f37dcc0` | 200 | 通过 |
| 26 | 标记自评 | PUT | `/api/v1/knowledge/entries/78/self-assessment` | 200 | 通过 |
| 27 | 我标记过的 | GET | `/api/v1/knowledge/self-assessments` | 200 | 通过 |
| 28 | 取消自评 | DELETE | `/api/v1/knowledge/entries/78/self-assessment` | 200 | 通过 |
| 29 | 建笔记 | POST | `/api/v1/notes` | 200 | 通过 |
| 30 | 笔记列表 | GET | `/api/v1/notes` | 200 | 通过 |
| 31 | 笔记详情 | GET | `/api/v1/notes/40` | 200 | 通过 |
| 32 | 改笔记 | PUT | `/api/v1/notes/40` | 200 | 通过 |
| 33 | 分享笔记 | POST | `/api/v1/notes/40/share` | 200 | 通过 |
| 34 | 我的分享 | GET | `/api/v1/notes/40/shares` | 200 | 通过 |
| 35 | 公开读分享（免鉴权） | GET | `/api/v1/public/shares/aee350bb-f2d2-4d9a-b0b2-cd2f3a47c321` | 200 | 通过 |
| 36 | 举报分享 | POST | `/api/v1/reports` | 200 | 通过 |
| 37 | 管理端举报列表 | GET | `/api/v1/notes/admin/reports` | 200 | 通过 |
| 38 | 下架（处理举报） | POST | `/api/v1/notes/admin/reports/29/takedown` | 200 | 通过 |
| 39 | 再分享一次（供撤回用） | POST | `/api/v1/notes/40/share` | 200 | 通过 |
| 40 | 撤回分享 | DELETE | `/api/v1/shares/9d46d345-26e2-4d5c-80ba-3f7ca570beb2` | 200 | 通过 |
| 41 | 删笔记 | DELETE | `/api/v1/notes/40` | 200 | 通过 |
| 42 | AI 凭据状态 | GET | `/api/v1/ai/credentials` | 200 | 通过 |
| 43 | 我的积分 | GET | `/api/v1/ai/credits` | 200 | 通过 |
| 44 | 读设置 | GET | `/api/v1/ai/settings` | 200 | 通过 |
| 45 | 写设置 | PUT | `/api/v1/ai/settings` | 200 | 通过 |
| 46 | 写记忆 | POST | `/api/v1/ai/memories` | 200 | 通过 |
| 47 | 读记忆 | GET | `/api/v1/ai/memories` | 200 | 通过 |
| 48 | 删单条记忆 | DELETE | `/api/v1/ai/memories/text/61` | 200 | 通过 |
| 49 | 写第二条记忆 | POST | `/api/v1/ai/memories` | 200 | 通过 |
| 50 | 清空并关闭记忆 | DELETE | `/api/v1/ai/memories` | 200 | 通过 |
| 51 | 重新打开记忆 | PUT | `/api/v1/ai/settings` | 200 | 通过 |
| 52 | 读精简（可能为空） | GET | `/api/v1/ai/summaries/78` | 200 | 通过 |
| 53 | 生成精简（桩） | POST | `/api/v1/ai/summaries` | 200 | 通过 |
| 54 | 新建会话 | POST | `/api/v1/ai/conversations` | 200 | 通过 |
| 55 | 会话列表 | GET | `/api/v1/ai/conversations` | 200 | 通过 |
| 56 | 会话消息（空） | GET | `/api/v1/ai/conversations/73/messages` | 200 | 通过 |
| 57 | 发消息（SSE） | POST | `/api/v1/ai/conversations/73/messages` | 200 | 通过 |
| 58 | 生成面试点评（桩） | POST | `/api/v1/ai/interviews/73/review` | 200 | 通过 |
| 59 | 我历次点评 | GET | `/api/v1/ai/interviews/evaluations` | 200 | 通过 |
| 60 | 管理端用量流水 | GET | `/api/v1/ai/admin/credits/audit` | 200 | 通过 |
| 61 | 管理端查某人积分 | GET | `/api/v1/ai/admin/credits/39` | 200 | 通过 |
| 62 | 管理端调分 | POST | `/api/v1/ai/admin/credits/39/adjust` | 200 | 通过 |
| 63 | 沙箱状态 | GET | `/api/v1/sandbox/status` | 200 | 通过 |
| 64 | 运行代码 | POST | `/api/v1/sandbox/run` | 503 | 通过 |
| 65 | 登出 | POST | `/api/v1/auth/logout` | 200 | 通过 |
| 66 | 注销账号 | POST | `/api/v1/me/deletion` | 200 | 通过 |
| 67 | 注销后 access 仍可用（JWT 固有取舍） | GET | `/api/v1/me` | 200 | 通过 |
| 68 | 注销后 access 仍返回 200（预期行为，不是缺陷） | - | `-` | 0 | 通过 |
| 69 | 注销后 refresh 失效 | POST | `/api/v1/auth/refresh` | 401 | 通过 |
