---
artifact: test-run
run_id: 2026-10-19T1511
layer: walkthrough
target: local
status: passed
started: 2026-10-19 15:11:52
---

# 全接口自查 2026-10-19 15:11:52

合计 **111** 步：通过 111，失败 0；覆盖端点 **101** 条。

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
| 13 | 改分类 | PUT | `/api/v1/knowledge/admin/categories/73` | 200 | 通过 |
| 14 | 建条目 | POST | `/api/v1/knowledge/admin/entries` | 200 | 通过 |
| 15 | 再建一条（供关系用） | POST | `/api/v1/knowledge/admin/entries` | 200 | 通过 |
| 16 | 管理端条目列表 | GET | `/api/v1/knowledge/admin/entries?size=5` | 200 | 通过 |
| 17 | 管理端条目详情 | GET | `/api/v1/knowledge/admin/entries/205` | 200 | 通过 |
| 18 | 改条目 | PUT | `/api/v1/knowledge/admin/entries/205` | 200 | 通过 |
| 19 | 建关系 | POST | `/api/v1/knowledge/admin/relations` | 200 | 通过 |
| 20 | 删关系 | DELETE | `/api/v1/knowledge/admin/relations/31` | 200 | 通过 |
| 21 | 概念列表 | GET | `/api/v1/knowledge/admin/concepts` | 200 | 通过 |
| 22 | 建概念 | POST | `/api/v1/knowledge/admin/concepts` | 200 | 通过 |
| 23 | 改概念 | PUT | `/api/v1/knowledge/admin/concepts/126` | 200 | 通过 |
| 24 | 挂条目 | POST | `/api/v1/knowledge/admin/concepts/126/entries` | 200 | 通过 |
| 25 | 解关联 | DELETE | `/api/v1/knowledge/admin/concepts/126/entries/205` | 200 | 通过 |
| 26 | 建概念二 | POST | `/api/v1/knowledge/admin/concepts` | 200 | 通过 |
| 27 | 建概念关系 | POST | `/api/v1/knowledge/admin/concept-edges` | 200 | 通过 |
| 28 | 删概念关系 | DELETE | `/api/v1/knowledge/admin/concept-edges/19` | 200 | 通过 |
| 29 | 删概念 | DELETE | `/api/v1/knowledge/admin/concepts/127` | 200 | 通过 |
| 30 | 上传附件 | POST | `/api/v1/knowledge/admin/files` | 200 | 通过 |
| 31 | 分类树 | GET | `/api/v1/knowledge/categories` | 200 | 通过 |
| 32 | 条目列表 | GET | `/api/v1/knowledge/entries?size=5` | 200 | 通过 |
| 33 | 条目详情 | GET | `/api/v1/knowledge/entries/205` | 200 | 通过 |
| 34 | 附件读取（免鉴权） | GET | `/api/v1/knowledge/files/fba6d0a6-e75c-429a-948a-023b6cb8ac6f` | 200 | 通过 |
| 35 | 标记自评 | PUT | `/api/v1/knowledge/entries/205/self-assessment` | 200 | 通过 |
| 36 | 我标记过的 | GET | `/api/v1/knowledge/self-assessments` | 200 | 通过 |
| 37 | 取消自评 | DELETE | `/api/v1/knowledge/entries/205/self-assessment` | 200 | 通过 |
| 38 | 记一次浏览 | POST | `/api/v1/knowledge/entries/205/view` | 200 | 通过 |
| 39 | 学习线清单 | GET | `/api/v1/learning/lines` | 200 | 通过 |
| 40 | 我的学习记录 | GET | `/api/v1/learning/progress` | 200 | 通过 |
| 41 | 单站记录 | GET | `/api/v1/learning/entries/205` | 200 | 通过 |
| 42 | 上报得分 | PUT | `/api/v1/learning/entries/205` | 200 | 通过 |
| 43 | 我的知识图谱 | GET | `/api/v1/learning/graph` | 200 | 通过 |
| 44 | 重置本站 | DELETE | `/api/v1/learning/entries/205` | 200 | 通过 |
| 45 | 建笔记 | POST | `/api/v1/notes` | 200 | 通过 |
| 46 | 笔记列表 | GET | `/api/v1/notes` | 200 | 通过 |
| 47 | 笔记详情 | GET | `/api/v1/notes/187` | 200 | 通过 |
| 48 | 改笔记 | PUT | `/api/v1/notes/187` | 200 | 通过 |
| 49 | 分享笔记 | POST | `/api/v1/notes/187/share` | 200 | 通过 |
| 50 | 我的分享 | GET | `/api/v1/notes/187/shares` | 200 | 通过 |
| 51 | 公开读分享（免鉴权） | GET | `/api/v1/public/shares/55e38a65-73fe-4858-a516-ae0ecd4bbe7a` | 200 | 通过 |
| 52 | 举报分享 | POST | `/api/v1/reports` | 200 | 通过 |
| 53 | 管理端举报列表 | GET | `/api/v1/notes/admin/reports` | 200 | 通过 |
| 54 | 下架（处理举报） | POST | `/api/v1/notes/admin/reports/73/takedown` | 200 | 通过 |
| 55 | 再分享一次（供撤回用） | POST | `/api/v1/notes/187/share` | 200 | 通过 |
| 56 | 撤回分享 | DELETE | `/api/v1/shares/4223f7c6-4ec7-45c9-90ad-7539a1d93205` | 200 | 通过 |
| 57 | 删笔记 | DELETE | `/api/v1/notes/187` | 200 | 通过 |
| 58 | AI 凭据状态 | GET | `/api/v1/ai/credentials` | 200 | 通过 |
| 59 | 我的积分 | GET | `/api/v1/ai/credits` | 200 | 通过 |
| 60 | 新建 MCP 凭据 | POST | `/api/v1/ai/mcp-tokens` | 200 | 通过 |
| 61 | MCP 凭据列表 | GET | `/api/v1/ai/mcp-tokens` | 200 | 通过 |
| 62 | 吊销 MCP 凭据 | DELETE | `/api/v1/ai/mcp-tokens/72` | 200 | 通过 |
| 63 | 读设置 | GET | `/api/v1/ai/settings` | 200 | 通过 |
| 64 | 写设置 | PUT | `/api/v1/ai/settings` | 200 | 通过 |
| 65 | 写记忆 | POST | `/api/v1/ai/memories` | 200 | 通过 |
| 66 | 读记忆 | GET | `/api/v1/ai/memories` | 200 | 通过 |
| 67 | 删单条记忆 | DELETE | `/api/v1/ai/memories/text/186` | 200 | 通过 |
| 68 | 写第二条记忆 | POST | `/api/v1/ai/memories` | 200 | 通过 |
| 69 | 清空并关闭记忆 | DELETE | `/api/v1/ai/memories` | 200 | 通过 |
| 70 | 重新打开记忆 | PUT | `/api/v1/ai/settings` | 200 | 通过 |
| 71 | 读精简（可能为空） | GET | `/api/v1/ai/summaries/205` | 200 | 通过 |
| 72 | 生成精简（桩） | POST | `/api/v1/ai/summaries` | 200 | 通过 |
| 73 | 新建会话 | POST | `/api/v1/ai/conversations` | 200 | 通过 |
| 74 | 会话列表 | GET | `/api/v1/ai/conversations` | 200 | 通过 |
| 75 | 会话消息（空） | GET | `/api/v1/ai/conversations/294/messages` | 200 | 通过 |
| 76 | 发消息（SSE） | POST | `/api/v1/ai/conversations/294/messages` | 200 | 通过 |
| 77 | 生成面试点评（桩） | POST | `/api/v1/ai/interviews/294/review` | 200 | 通过 |
| 78 | 我历次点评 | GET | `/api/v1/ai/interviews/evaluations` | 200 | 通过 |
| 79 | 上传并解析简历（桩） | POST | `/api/v1/ai/resumes/parse` | 200 | 通过 |
| 80 | 新建主简历 | POST | `/api/v1/ai/resumes` | 200 | 通过 |
| 81 | 简历列表 | GET | `/api/v1/ai/resumes` | 200 | 通过 |
| 82 | 读简历 | GET | `/api/v1/ai/resumes/68` | 200 | 通过 |
| 83 | 改简历标题 | PUT | `/api/v1/ai/resumes/68/title` | 200 | 通过 |
| 84 | 设为默认 | PUT | `/api/v1/ai/resumes/68/default` | 200 | 通过 |
| 85 | 体检 | GET | `/api/v1/ai/resumes/68/checkup` | 200 | 通过 |
| 86 | 保存简历 | PUT | `/api/v1/ai/resumes/68` | 200 | 通过 |
| 87 | 编辑历史 | GET | `/api/v1/ai/resumes/68/history` | 200 | 通过 |
| 88 | 编辑历史（取列表） | GET | `/api/v1/ai/resumes/68/history` | 200 | 通过 |
| 89 | 读某一版 | GET | `/api/v1/ai/resumes/68/history/68` | 200 | 通过 |
| 90 | 回滚到某一版 | POST | `/api/v1/ai/resumes/68/history/68/restore?revision=2` | 200 | 通过 |
| 91 | 建岗位版本 | POST | `/api/v1/ai/resumes/68/versions` | 200 | 通过 |
| 92 | 版本列表 | GET | `/api/v1/ai/resumes/68/versions` | 200 | 通过 |
| 93 | 分析岗位（桩） | POST | `/api/v1/ai/versions/37/analysis` | 200 | 通过 |
| 94 | 生成提案（桩） | POST | `/api/v1/ai/versions/37/proposals` | 200 | 通过 |
| 95 | 提案列表 | GET | `/api/v1/ai/versions/37/proposals` | 200 | 通过 |
| 96 | 采纳一条提案 | PATCH | `/api/v1/ai/proposals/37` | 200 | 通过 |
| 97 | 定稿 | POST | `/api/v1/ai/versions/37/finalize` | 200 | 通过 |
| 98 | 读版本 | GET | `/api/v1/ai/versions/37` | 200 | 通过 |
| 99 | 删版本 | DELETE | `/api/v1/ai/versions/37` | 200 | 通过 |
| 100 | 删简历 | DELETE | `/api/v1/ai/resumes/68` | 200 | 通过 |
| 101 | 回滚后的 revision=3（保存与回滚都各推进一次） | - | `-` | 0 | 通过 |
| 102 | 管理端用量流水 | GET | `/api/v1/ai/admin/credits/audit` | 200 | 通过 |
| 103 | 管理端查某人积分 | GET | `/api/v1/ai/admin/credits/230` | 200 | 通过 |
| 104 | 管理端调分 | POST | `/api/v1/ai/admin/credits/230/adjust` | 200 | 通过 |
| 105 | 沙箱状态 | GET | `/api/v1/sandbox/status` | 200 | 通过 |
| 106 | 运行代码 | POST | `/api/v1/sandbox/run` | 503 | 通过 |
| 107 | 登出 | POST | `/api/v1/auth/logout` | 200 | 通过 |
| 108 | 注销账号 | POST | `/api/v1/me/deletion` | 200 | 通过 |
| 109 | 注销后 access 仍可用（JWT 固有取舍） | GET | `/api/v1/me` | 200 | 通过 |
| 110 | 注销后 access 仍返回 200（预期行为，不是缺陷） | - | `-` | 0 | 通过 |
| 111 | 注销后 refresh 失效 | POST | `/api/v1/auth/refresh` | 401 | 通过 |
