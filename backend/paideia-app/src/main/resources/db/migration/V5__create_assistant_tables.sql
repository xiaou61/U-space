-- AI 助手：会话与消息（WORK-006 TASK-001）。
--
-- **不建跨模块外键**，与 V4 的约定一致：本项目的表只在 SQL 里声明归属列，不在数据库层面
-- 引用别的模块的表。测试也依赖这条——集成测试用"任意数字身份"造数据（如 `"91" + nanoTime`），
-- 库里并没有对应的 users 行；一旦加外键，每个用例都得先造真实用户，而给**已有**表加外键
-- 更会直接把 AiSummaryIntegrationTest 打红。
--
-- 因此"注销即级联硬删"由**应用层显式删除**保证：注销流程按一张固定的表清单逐表删净。
-- 那份清单连同 AC-014 的用例（注销后逐表断言行数为 0）一起，才是"删干净"的真正护栏——
-- 新增用户私有表时忘了登记，用例会红。
--
-- 消息表不带 user_id：它随会话一起删（会话删了消息就没有意义）。

CREATE TABLE ai_conversations (
  id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id    BIGINT UNSIGNED NOT NULL,
  -- 可选上下文。对不上就当"没有上下文"，不因此删掉用户自己的对话。
  entry_id   BIGINT UNSIGNED NULL,
  -- 滑动过期：每追加一条消息就往后推，避免"聊到一半会话突然过期"。
  -- 时间由应用按 UTC 写入（不用 NOW()：库的会话时区是 +08:00，用库时间会整体偏 8 小时）。
  expires_at DATETIME(3)     NOT NULL,
  created_at DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_ai_conversation_user (user_id, updated_at)
) ENGINE = InnoDB;

CREATE TABLE ai_messages (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  conversation_id BIGINT UNSIGNED NOT NULL,
  -- user | assistant。**思考过程不入库**，所以没有第三个角色（见 design 的实现选择）。
  role            VARCHAR(16)     NOT NULL,
  content         MEDIUMTEXT      NOT NULL,
  -- 只有助手消息有；用户消息为 NULL。BYOK 下用量要返还给用户，所以随消息存一份。
  token_usage     JSON            NULL,
  created_at      DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  -- 取最近 N 条走 (conversation_id, id)：id 单调，比按 created_at 排序更准（同毫秒的并列）。
  KEY idx_ai_message_conversation (conversation_id, id)
) ENGINE = InnoDB;
