---
artifact: test-run
run_id: 2026-10-21T1754
layer: walkthrough
target: local
status: passed
started: 2026-10-21 17:54:05
---

# 全接口自查 2026-10-21 17:54:05

合计 **145** 步：通过 145，失败 0；覆盖端点 **134** 条。

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
| 13 | 改分类 | PUT | `/api/v1/knowledge/admin/categories/901` | 200 | 通过 |
| 14 | 建条目 | POST | `/api/v1/knowledge/admin/entries` | 200 | 通过 |
| 15 | 再建一条（供关系用） | POST | `/api/v1/knowledge/admin/entries` | 200 | 通过 |
| 16 | 管理端条目列表 | GET | `/api/v1/knowledge/admin/entries?size=5` | 200 | 通过 |
| 17 | 管理端条目详情 | GET | `/api/v1/knowledge/admin/entries/26` | 200 | 通过 |
| 18 | 改条目 | PUT | `/api/v1/knowledge/admin/entries/26` | 200 | 通过 |
| 19 | 建关系 | POST | `/api/v1/knowledge/admin/relations` | 200 | 通过 |
| 20 | 删关系 | DELETE | `/api/v1/knowledge/admin/relations/3` | 200 | 通过 |
| 21 | 建概念 | POST | `/api/v1/knowledge/admin/concepts` | 200 | 通过 |
| 22 | 改概念 | PUT | `/api/v1/knowledge/admin/concepts/1` | 200 | 通过 |
| 23 | 概念列表 | GET | `/api/v1/knowledge/admin/concepts` | 200 | 通过 |
| 24 | 挂条目 | POST | `/api/v1/knowledge/admin/concepts/1/entries` | 200 | 通过 |
| 25 | 解关联 | DELETE | `/api/v1/knowledge/admin/concepts/1/entries/26` | 200 | 通过 |
| 26 | 建概念二 | POST | `/api/v1/knowledge/admin/concepts` | 200 | 通过 |
| 27 | 建概念关系 | POST | `/api/v1/knowledge/admin/concept-edges` | 200 | 通过 |
| 28 | 删概念关系 | DELETE | `/api/v1/knowledge/admin/concept-edges/1` | 200 | 通过 |
| 29 | 删概念 | DELETE | `/api/v1/knowledge/admin/concepts/2` | 200 | 通过 |
| 30 | 上传附件 | POST | `/api/v1/knowledge/admin/files` | 200 | 通过 |
| 31 | 分类树 | GET | `/api/v1/knowledge/categories` | 200 | 通过 |
| 32 | 条目列表 | GET | `/api/v1/knowledge/entries?size=5` | 200 | 通过 |
| 33 | 条目详情 | GET | `/api/v1/knowledge/entries/26` | 200 | 通过 |
| 34 | 附件读取（免鉴权） | GET | `/api/v1/knowledge/files/6d804e08-56d3-42e0-84e5-ee7d5c6477e7` | 200 | 通过 |
| 35 | 标记自评 | PUT | `/api/v1/knowledge/entries/26/self-assessment` | 200 | 通过 |
| 36 | 我标记过的 | GET | `/api/v1/knowledge/self-assessments` | 200 | 通过 |
| 37 | 取消自评 | DELETE | `/api/v1/knowledge/entries/26/self-assessment` | 200 | 通过 |
| 38 | 记一次浏览 | POST | `/api/v1/knowledge/entries/26/view` | 200 | 通过 |
| 39 | 学习线清单 | GET | `/api/v1/learning/lines` | 200 | 通过 |
| 40 | 我的学习记录 | GET | `/api/v1/learning/progress` | 200 | 通过 |
| 41 | 单站记录 | GET | `/api/v1/learning/entries/26` | 200 | 通过 |
| 42 | 上报得分 | PUT | `/api/v1/learning/entries/26` | 200 | 通过 |
| 43 | 我的知识图谱 | GET | `/api/v1/learning/graph` | 200 | 通过 |
| 44 | 重置本站 | DELETE | `/api/v1/learning/entries/26` | 200 | 通过 |
| 45 | 导入学习轨道 | POST | `/api/v1/knowledge/admin/tracks` | 200 | 通过 |
| 46 | 轨道清单 | GET | `/api/v1/learn/tracks` | 200 | 通过 |
| 47 | 轨道关卡地图 | GET | `/api/v1/learn/tracks/walk-track-538206500` | 200 | 通过 |
| 48 | 建笔记 | POST | `/api/v1/notes` | 200 | 通过 |
| 49 | 笔记列表 | GET | `/api/v1/notes` | 200 | 通过 |
| 50 | 笔记详情 | GET | `/api/v1/notes/3` | 200 | 通过 |
| 51 | 改笔记 | PUT | `/api/v1/notes/3` | 200 | 通过 |
| 52 | 分享笔记 | POST | `/api/v1/notes/3/share` | 200 | 通过 |
| 53 | 我的分享 | GET | `/api/v1/notes/3/shares` | 200 | 通过 |
| 54 | 公开读分享（免鉴权） | GET | `/api/v1/public/shares/0d273701-2a97-4bfe-a779-5eeaa1e00b32` | 200 | 通过 |
| 55 | 举报分享 | POST | `/api/v1/reports` | 200 | 通过 |
| 56 | 管理端举报列表 | GET | `/api/v1/notes/admin/reports` | 200 | 通过 |
| 57 | 下架（处理举报） | POST | `/api/v1/notes/admin/reports/3/takedown` | 200 | 通过 |
| 58 | 再分享一次（供撤回用） | POST | `/api/v1/notes/3/share` | 200 | 通过 |
| 59 | 撤回分享 | DELETE | `/api/v1/shares/c3ee8793-5c8b-4495-9f43-c71e79a119ca` | 200 | 通过 |
| 60 | 删笔记 | DELETE | `/api/v1/notes/3` | 200 | 通过 |
| 61 | AI 凭据状态 | GET | `/api/v1/ai/credentials` | 200 | 通过 |
| 62 | 我的积分 | GET | `/api/v1/ai/credits` | 200 | 通过 |
| 63 | 新建 MCP 凭据 | POST | `/api/v1/ai/mcp-tokens` | 200 | 通过 |
| 64 | MCP 凭据列表 | GET | `/api/v1/ai/mcp-tokens` | 200 | 通过 |
| 65 | 吊销 MCP 凭据 | DELETE | `/api/v1/ai/mcp-tokens/3` | 200 | 通过 |
| 66 | 读设置 | GET | `/api/v1/ai/settings` | 200 | 通过 |
| 67 | 写设置 | PUT | `/api/v1/ai/settings` | 200 | 通过 |
| 68 | 写记忆 | POST | `/api/v1/ai/memories` | 200 | 通过 |
| 69 | 读记忆 | GET | `/api/v1/ai/memories` | 200 | 通过 |
| 70 | 删单条记忆 | DELETE | `/api/v1/ai/memories/text/3` | 200 | 通过 |
| 71 | 写第二条记忆 | POST | `/api/v1/ai/memories` | 200 | 通过 |
| 72 | 清空并关闭记忆 | DELETE | `/api/v1/ai/memories` | 200 | 通过 |
| 73 | 重新打开记忆 | PUT | `/api/v1/ai/settings` | 200 | 通过 |
| 74 | 读精简（可能为空） | GET | `/api/v1/ai/summaries/26` | 200 | 通过 |
| 75 | 生成精简（桩） | POST | `/api/v1/ai/summaries` | 200 | 通过 |
| 76 | 新建会话 | POST | `/api/v1/ai/conversations` | 200 | 通过 |
| 77 | 会话列表 | GET | `/api/v1/ai/conversations` | 200 | 通过 |
| 78 | 会话消息（空） | GET | `/api/v1/ai/conversations/6/messages` | 200 | 通过 |
| 79 | 发消息（SSE） | POST | `/api/v1/ai/conversations/6/messages` | 200 | 通过 |
| 80 | 生成面试点评（桩） | POST | `/api/v1/ai/interviews/6/review` | 200 | 通过 |
| 81 | 我历次点评 | GET | `/api/v1/ai/interviews/evaluations` | 200 | 通过 |
| 82 | 上传并解析简历（桩） | POST | `/api/v1/ai/resumes/parse` | 200 | 通过 |
| 83 | 新建主简历 | POST | `/api/v1/ai/resumes` | 200 | 通过 |
| 84 | 简历列表 | GET | `/api/v1/ai/resumes` | 200 | 通过 |
| 85 | 读简历 | GET | `/api/v1/ai/resumes/3` | 200 | 通过 |
| 86 | 改简历标题 | PUT | `/api/v1/ai/resumes/3/title` | 200 | 通过 |
| 87 | 设为默认 | PUT | `/api/v1/ai/resumes/3/default` | 200 | 通过 |
| 88 | 体检 | GET | `/api/v1/ai/resumes/3/checkup` | 200 | 通过 |
| 89 | 保存简历 | PUT | `/api/v1/ai/resumes/3` | 200 | 通过 |
| 90 | 编辑历史 | GET | `/api/v1/ai/resumes/3/history` | 200 | 通过 |
| 91 | 编辑历史（取列表） | GET | `/api/v1/ai/resumes/3/history` | 200 | 通过 |
| 92 | 读某一版 | GET | `/api/v1/ai/resumes/3/history/3` | 200 | 通过 |
| 93 | 回滚到某一版 | POST | `/api/v1/ai/resumes/3/history/3/restore?revision=2` | 200 | 通过 |
| 94 | 建岗位版本 | POST | `/api/v1/ai/resumes/3/versions` | 200 | 通过 |
| 95 | 版本列表 | GET | `/api/v1/ai/resumes/3/versions` | 200 | 通过 |
| 96 | 分析岗位（桩） | POST | `/api/v1/ai/versions/3/analysis` | 200 | 通过 |
| 97 | 生成提案（桩） | POST | `/api/v1/ai/versions/3/proposals` | 200 | 通过 |
| 98 | 提案列表 | GET | `/api/v1/ai/versions/3/proposals` | 200 | 通过 |
| 99 | 采纳一条提案 | PATCH | `/api/v1/ai/proposals/3` | 200 | 通过 |
| 100 | 定稿 | POST | `/api/v1/ai/versions/3/finalize` | 200 | 通过 |
| 101 | 读版本 | GET | `/api/v1/ai/versions/3` | 200 | 通过 |
| 102 | 删版本 | DELETE | `/api/v1/ai/versions/3` | 200 | 通过 |
| 103 | 删简历 | DELETE | `/api/v1/ai/resumes/3` | 200 | 通过 |
| 104 | 回滚后的 revision=3（保存与回滚都各推进一次） | - | `-` | 0 | 通过 |
| 105 | 训练营用的简历 | POST | `/api/v1/ai/resumes` | 200 | 通过 |
| 106 | 开训练营（桩） | POST | `/api/v1/ai/camps` | 200 | 通过 |
| 107 | 我的训练营 | GET | `/api/v1/ai/camps` | 200 | 通过 |
| 108 | 营概览 | GET | `/api/v1/ai/camps/3` | 200 | 通过 |
| 109 | 取诊断卷 | GET | `/api/v1/ai/camps/3/items?form=diagnostic` | 200 | 通过 |
| 110 | 交诊断卷 | POST | `/api/v1/ai/camps/3/submit` | 200 | 通过 |
| 111 | 诊断结果 | GET | `/api/v1/ai/camps/3/result?form=diagnostic` | 200 | 通过 |
| 112 | 出计划 | POST | `/api/v1/ai/camps/3/plan` | 200 | 通过 |
| 113 | 读计划 | GET | `/api/v1/ai/camps/3/plan` | 200 | 通过 |
| 114 | 勾选任务 | PATCH | `/api/v1/ai/camps/3/tasks/3` | 200 | 通过 |
| 115 | 复测出卷（桩） | POST | `/api/v1/ai/camps/3/retest` | 200 | 通过 |
| 116 | 取复测卷 | GET | `/api/v1/ai/camps/3/items?form=retest` | 200 | 通过 |
| 117 | 交复测卷 | POST | `/api/v1/ai/camps/3/submit` | 200 | 通过 |
| 118 | 复测结果（并排） | GET | `/api/v1/ai/camps/3/result?form=retest` | 200 | 通过 |
| 119 | 删营 | DELETE | `/api/v1/ai/camps/3` | 200 | 通过 |
| 120 | 管理端用量流水 | GET | `/api/v1/ai/admin/credits/audit` | 200 | 通过 |
| 121 | 管理端用量流水（分页筛选） | GET | `/api/v1/ai/admin/credits/ledger?page=1&size=10` | 200 | 通过 |
| 122 | 管理端查某人积分 | GET | `/api/v1/ai/admin/credits/26` | 200 | 通过 |
| 123 | 管理端用户列表 | GET | `/api/v1/account/admin/users?page=1&size=10` | 200 | 通过 |
| 124 | 管理端用户详情 | GET | `/api/v1/account/admin/users/27` | 200 | 通过 |
| 125 | 管理端改角色 | POST | `/api/v1/account/admin/users/27/role` | 200 | 通过 |
| 126 | 管理端调积分（用户管理） | POST | `/api/v1/account/admin/users/27/credits` | 200 | 通过 |
| 127 | 管理端删号 | DELETE | `/api/v1/account/admin/users/27` | 200 | 通过 |
| 128 | 管理端调分 | POST | `/api/v1/ai/admin/credits/26/adjust` | 200 | 通过 |
| 129 | AI 调用概览 | GET | `/api/v1/ai/admin/runs/overview` | 200 | 通过 |
| 130 | AI 调用明细 | GET | `/api/v1/ai/admin/runs` | 200 | 通过 |
| 131 | AI 调用筛选项 | GET | `/api/v1/ai/admin/runs/meta` | 200 | 通过 |
| 132 | AI 调用详情 | GET | `/api/v1/ai/admin/runs/selfcheck538206500` | 200 | 通过 |
| 133 | 沙箱状态 | GET | `/api/v1/sandbox/status` | 200 | 通过 |
| 134 | 逐步录制 | POST | `/api/v1/sandbox/trace` | 503 | 通过 |
| 135 | 运行代码 | POST | `/api/v1/sandbox/run` | 503 | 通过 |
| 136 | 登出 | POST | `/api/v1/auth/logout` | 200 | 通过 |
| 137 | 注销账号 | POST | `/api/v1/me/deletion` | 200 | 通过 |
| 138 | 注销后 access 仍可用（JWT 固有取舍） | GET | `/api/v1/me` | 200 | 通过 |
| 139 | 注销后 access 仍返回 200（预期行为，不是缺陷） | - | `-` | 0 | 通过 |
| 140 | 注销后 refresh 失效 | POST | `/api/v1/auth/refresh` | 401 | 通过 |
| 141 | 运维摘要 | GET | `/api/v1/ops/admin/summary` | 200 | 通过 |
| 142 | 速递当日 | GET | `/api/v1/digest` | 200 | 通过 |
| 143 | 速递日期列表 | GET | `/api/v1/digest/dates` | 200 | 通过 |
| 144 | 速递周报 | GET | `/api/v1/digest/weekly` | 200 | 通过 |
| 145 | 管理端速递清单 | GET | `/api/v1/digest/admin/items` | 200 | 通过 |
