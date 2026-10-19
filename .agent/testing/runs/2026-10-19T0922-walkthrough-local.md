---
artifact: test-run
run_id: 2026-10-19T0922
layer: walkthrough
target: local
status: passed
started: 2026-10-19 09:22:45
---

# 全接口自查 2026-10-19 09:22:45

合计 **95** 步：通过 95，失败 0；覆盖端点 **86** 条。

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
| 13 | 改分类 | PUT | `/api/v1/knowledge/admin/categories/11` | 200 | 通过 |
| 14 | 建条目 | POST | `/api/v1/knowledge/admin/entries` | 200 | 通过 |
| 15 | 再建一条（供关系用） | POST | `/api/v1/knowledge/admin/entries` | 200 | 通过 |
| 16 | 管理端条目列表 | GET | `/api/v1/knowledge/admin/entries?size=5` | 200 | 通过 |
| 17 | 管理端条目详情 | GET | `/api/v1/knowledge/admin/entries/26` | 200 | 通过 |
| 18 | 改条目 | PUT | `/api/v1/knowledge/admin/entries/26` | 200 | 通过 |
| 19 | 建关系 | POST | `/api/v1/knowledge/admin/relations` | 200 | 通过 |
| 20 | 删关系 | DELETE | `/api/v1/knowledge/admin/relations/3` | 200 | 通过 |
| 21 | 上传附件 | POST | `/api/v1/knowledge/admin/files` | 200 | 通过 |
| 22 | 分类树 | GET | `/api/v1/knowledge/categories` | 200 | 通过 |
| 23 | 条目列表 | GET | `/api/v1/knowledge/entries?size=5` | 200 | 通过 |
| 24 | 条目详情 | GET | `/api/v1/knowledge/entries/26` | 200 | 通过 |
| 25 | 附件读取（免鉴权） | GET | `/api/v1/knowledge/files/d6b8a2f5-88e4-429f-8f08-6f7f9e8c3f7a` | 200 | 通过 |
| 26 | 标记自评 | PUT | `/api/v1/knowledge/entries/26/self-assessment` | 200 | 通过 |
| 27 | 我标记过的 | GET | `/api/v1/knowledge/self-assessments` | 200 | 通过 |
| 28 | 取消自评 | DELETE | `/api/v1/knowledge/entries/26/self-assessment` | 200 | 通过 |
| 29 | 建笔记 | POST | `/api/v1/notes` | 200 | 通过 |
| 30 | 笔记列表 | GET | `/api/v1/notes` | 200 | 通过 |
| 31 | 笔记详情 | GET | `/api/v1/notes/3` | 200 | 通过 |
| 32 | 改笔记 | PUT | `/api/v1/notes/3` | 200 | 通过 |
| 33 | 分享笔记 | POST | `/api/v1/notes/3/share` | 200 | 通过 |
| 34 | 我的分享 | GET | `/api/v1/notes/3/shares` | 200 | 通过 |
| 35 | 公开读分享（免鉴权） | GET | `/api/v1/public/shares/39da626b-a984-44da-8728-9f227fca1689` | 200 | 通过 |
| 36 | 举报分享 | POST | `/api/v1/reports` | 200 | 通过 |
| 37 | 管理端举报列表 | GET | `/api/v1/notes/admin/reports` | 200 | 通过 |
| 38 | 下架（处理举报） | POST | `/api/v1/notes/admin/reports/3/takedown` | 200 | 通过 |
| 39 | 再分享一次（供撤回用） | POST | `/api/v1/notes/3/share` | 200 | 通过 |
| 40 | 撤回分享 | DELETE | `/api/v1/shares/4323953f-bdd5-441a-934e-4c17787fa038` | 200 | 通过 |
| 41 | 删笔记 | DELETE | `/api/v1/notes/3` | 200 | 通过 |
| 42 | AI 凭据状态 | GET | `/api/v1/ai/credentials` | 200 | 通过 |
| 43 | 我的积分 | GET | `/api/v1/ai/credits` | 200 | 通过 |
| 44 | 新建 MCP 凭据 | POST | `/api/v1/ai/mcp-tokens` | 200 | 通过 |
| 45 | MCP 凭据列表 | GET | `/api/v1/ai/mcp-tokens` | 200 | 通过 |
| 46 | 吊销 MCP 凭据 | DELETE | `/api/v1/ai/mcp-tokens/3` | 200 | 通过 |
| 47 | 读设置 | GET | `/api/v1/ai/settings` | 200 | 通过 |
| 48 | 写设置 | PUT | `/api/v1/ai/settings` | 200 | 通过 |
| 49 | 写记忆 | POST | `/api/v1/ai/memories` | 200 | 通过 |
| 50 | 读记忆 | GET | `/api/v1/ai/memories` | 200 | 通过 |
| 51 | 删单条记忆 | DELETE | `/api/v1/ai/memories/text/3` | 200 | 通过 |
| 52 | 写第二条记忆 | POST | `/api/v1/ai/memories` | 200 | 通过 |
| 53 | 清空并关闭记忆 | DELETE | `/api/v1/ai/memories` | 200 | 通过 |
| 54 | 重新打开记忆 | PUT | `/api/v1/ai/settings` | 200 | 通过 |
| 55 | 读精简（可能为空） | GET | `/api/v1/ai/summaries/26` | 200 | 通过 |
| 56 | 生成精简（桩） | POST | `/api/v1/ai/summaries` | 200 | 通过 |
| 57 | 新建会话 | POST | `/api/v1/ai/conversations` | 200 | 通过 |
| 58 | 会话列表 | GET | `/api/v1/ai/conversations` | 200 | 通过 |
| 59 | 会话消息（空） | GET | `/api/v1/ai/conversations/6/messages` | 200 | 通过 |
| 60 | 发消息（SSE） | POST | `/api/v1/ai/conversations/6/messages` | 200 | 通过 |
| 61 | 生成面试点评（桩） | POST | `/api/v1/ai/interviews/6/review` | 200 | 通过 |
| 62 | 我历次点评 | GET | `/api/v1/ai/interviews/evaluations` | 200 | 通过 |
| 63 | 上传并解析简历（桩） | POST | `/api/v1/ai/resumes/parse` | 200 | 通过 |
| 64 | 新建主简历 | POST | `/api/v1/ai/resumes` | 200 | 通过 |
| 65 | 简历列表 | GET | `/api/v1/ai/resumes` | 200 | 通过 |
| 66 | 读简历 | GET | `/api/v1/ai/resumes/3` | 200 | 通过 |
| 67 | 改简历标题 | PUT | `/api/v1/ai/resumes/3/title` | 200 | 通过 |
| 68 | 设为默认 | PUT | `/api/v1/ai/resumes/3/default` | 200 | 通过 |
| 69 | 体检 | GET | `/api/v1/ai/resumes/3/checkup` | 200 | 通过 |
| 70 | 保存简历 | PUT | `/api/v1/ai/resumes/3` | 200 | 通过 |
| 71 | 编辑历史 | GET | `/api/v1/ai/resumes/3/history` | 200 | 通过 |
| 72 | 编辑历史（取列表） | GET | `/api/v1/ai/resumes/3/history` | 200 | 通过 |
| 73 | 读某一版 | GET | `/api/v1/ai/resumes/3/history/3` | 200 | 通过 |
| 74 | 回滚到某一版 | POST | `/api/v1/ai/resumes/3/history/3/restore?revision=2` | 200 | 通过 |
| 75 | 建岗位版本 | POST | `/api/v1/ai/resumes/3/versions` | 200 | 通过 |
| 76 | 版本列表 | GET | `/api/v1/ai/resumes/3/versions` | 200 | 通过 |
| 77 | 分析岗位（桩） | POST | `/api/v1/ai/versions/3/analysis` | 200 | 通过 |
| 78 | 生成提案（桩） | POST | `/api/v1/ai/versions/3/proposals` | 200 | 通过 |
| 79 | 提案列表 | GET | `/api/v1/ai/versions/3/proposals` | 200 | 通过 |
| 80 | 采纳一条提案 | PATCH | `/api/v1/ai/proposals/3` | 200 | 通过 |
| 81 | 定稿 | POST | `/api/v1/ai/versions/3/finalize` | 200 | 通过 |
| 82 | 读版本 | GET | `/api/v1/ai/versions/3` | 200 | 通过 |
| 83 | 删版本 | DELETE | `/api/v1/ai/versions/3` | 200 | 通过 |
| 84 | 删简历 | DELETE | `/api/v1/ai/resumes/3` | 200 | 通过 |
| 85 | 回滚后的 revision=3（保存与回滚都各推进一次） | - | `-` | 0 | 通过 |
| 86 | 管理端用量流水 | GET | `/api/v1/ai/admin/credits/audit` | 200 | 通过 |
| 87 | 管理端查某人积分 | GET | `/api/v1/ai/admin/credits/14` | 200 | 通过 |
| 88 | 管理端调分 | POST | `/api/v1/ai/admin/credits/14/adjust` | 200 | 通过 |
| 89 | 沙箱状态 | GET | `/api/v1/sandbox/status` | 200 | 通过 |
| 90 | 运行代码 | POST | `/api/v1/sandbox/run` | 503 | 通过 |
| 91 | 登出 | POST | `/api/v1/auth/logout` | 200 | 通过 |
| 92 | 注销账号 | POST | `/api/v1/me/deletion` | 200 | 通过 |
| 93 | 注销后 access 仍可用（JWT 固有取舍） | GET | `/api/v1/me` | 200 | 通过 |
| 94 | 注销后 access 仍返回 200（预期行为，不是缺陷） | - | `-` | 0 | 通过 |
| 95 | 注销后 refresh 失效 | POST | `/api/v1/auth/refresh` | 401 | 通过 |
